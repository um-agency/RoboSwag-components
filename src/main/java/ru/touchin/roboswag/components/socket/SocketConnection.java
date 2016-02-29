package ru.touchin.roboswag.components.socket;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
import java.util.Map;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.android.RxAndroidUtils;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 29/02/16.
 * TODO: description
 */
public abstract class SocketConnection {

    private final BehaviorSubject<State> stateSubject = BehaviorSubject.create(State.DISCONNECTED);
    private final Scheduler scheduler = RxAndroidUtils.createLooperScheduler();
    private final Map<SocketEvent, Observable> messagesObservableCache = new HashMap<>();

    @Nullable
    private Observable<Socket> socketObservable;

    @NonNull
    public Scheduler getScheduler() {
        return scheduler;
    }

    protected Observable<Socket> getSocket() {
        synchronized (scheduler) {
            if (socketObservable == null) {
                socketObservable = createSocketObservable();
            }
        }
        return socketObservable;
    }

    protected abstract Socket createSocket() throws Exception;

    private Observable<Socket> createSocketObservable() {
        return Observable
                .<Socket>create(subscriber -> {
                    try {
                        final Socket socket = createSocket();
                        socket.on(Socket.EVENT_CONNECT, args -> stateSubject.onNext(State.CONNECTED));
                        socket.on(Socket.EVENT_CONNECTING, args -> stateSubject.onNext(State.CONNECTING));
                        socket.on(Socket.EVENT_CONNECT_ERROR, args -> stateSubject.onNext(State.CONNECTION_ERROR));
                        socket.on(Socket.EVENT_CONNECT_TIMEOUT, args -> stateSubject.onNext(State.CONNECTION_ERROR));
                        socket.on(Socket.EVENT_DISCONNECT, args -> stateSubject.onNext(State.DISCONNECTED));
                        socket.on(Socket.EVENT_RECONNECT_ATTEMPT, args -> stateSubject.onNext(State.CONNECTING));
                        socket.on(Socket.EVENT_RECONNECTING, args -> stateSubject.onNext(State.CONNECTING));
                        socket.on(Socket.EVENT_RECONNECT, args -> stateSubject.onNext(State.CONNECTED));
                        socket.on(Socket.EVENT_RECONNECT_ERROR, args -> stateSubject.onNext(State.CONNECTION_ERROR));
                        socket.on(Socket.EVENT_RECONNECT_FAILED, args -> stateSubject.onNext(State.CONNECTION_ERROR));
                        subscriber.onNext(socket);
                    } catch (final Exception exception) {
                        Lc.assertion(exception);
                    }
                    subscriber.onCompleted();
                })
                .subscribeOn(scheduler)
                .switchMap(socket -> Observable.just(socket)
                        .doOnSubscribe(socket::connect)
                        .doOnUnsubscribe(socket::disconnect))
                .replay(1)
                .refCount();
    }

    @SuppressWarnings("unchecked")
    protected <T> Observable<T> observeEvent(@NonNull final SocketEvent<T> socketEvent) {
        Observable result;
        synchronized (scheduler) {
            result = messagesObservableCache.get(socketEvent);
            if (result != null) {
                result = getSocket()
                        .switchMap(socket -> Observable
                                .<T>create(subscriber -> socket.on(socketEvent.getName(),
                                        new SocketListener<>(socketEvent, subscriber::onNext)))
                                .doOnUnsubscribe(() -> socket.off(socketEvent.getName())));
                messagesObservableCache.put(socketEvent, result);
            }
        }
        return result;
    }

    public Observable<State> observeSocketState() {
        return stateSubject.distinctUntilChanged();
    }

    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        CONNECTION_ERROR
    }

    public class SocketListener<T> implements Emitter.Listener {

        @NonNull
        private final SocketEvent<T> socketEvent;
        @NonNull
        private final Action1<T> action;

        public SocketListener(@NonNull final SocketEvent<T> socketEvent,
                              @NonNull final Action1<T> action) {
            this.socketEvent = socketEvent;
            this.action = action;
        }

        @Override
        public void call(final Object... args) {
            try {
                if (args != null) {
                    final String response = args[0].toString();
                    Lc.d("Got socket message: %s", response);
                    T message = socketEvent.parse(response);
                    if (socketEvent.getEventDataHandler() != null) {
                        socketEvent.getEventDataHandler().handleMessage(message);
                    }
                    action.call(message);
                }
            } catch (final RuntimeException throwable) {
                Lc.assertion(throwable);
            } catch (final JsonProcessingException exception) {
                Lc.assertion(exception);
            } catch (final Exception exception) {
                Lc.e("Socket processing error", exception);
            }
        }

    }

}
