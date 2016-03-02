/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
                        socket.on(Socket.EVENT_CONNECT, args -> changeSocketState(State.CONNECTED));
                        socket.on(Socket.EVENT_CONNECTING, args -> changeSocketState(State.CONNECTING));
                        socket.on(Socket.EVENT_CONNECT_ERROR, args -> changeSocketState(State.CONNECTION_ERROR));
                        socket.on(Socket.EVENT_CONNECT_TIMEOUT, args -> changeSocketState(State.CONNECTION_ERROR));
                        socket.on(Socket.EVENT_DISCONNECT, args -> changeSocketState(State.DISCONNECTED));
                        socket.on(Socket.EVENT_RECONNECT_ATTEMPT, args -> changeSocketState(State.CONNECTING));
                        socket.on(Socket.EVENT_RECONNECTING, args -> changeSocketState(State.CONNECTING));
                        socket.on(Socket.EVENT_RECONNECT, args -> changeSocketState(State.CONNECTED));
                        socket.on(Socket.EVENT_RECONNECT_ERROR, args -> changeSocketState(State.CONNECTION_ERROR));
                        socket.on(Socket.EVENT_RECONNECT_FAILED, args -> changeSocketState(State.CONNECTION_ERROR));
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

    private void changeSocketState(@NonNull final State state) {
        stateSubject.onNext(state);
        Lc.d("Socket state changed: %s", state);
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
                Lc.e(exception, "Socket processing error");
            }
        }

    }

}
