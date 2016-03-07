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

package ru.touchin.roboswag.components.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.facebook.cache.common.CacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

import javax.annotation.Nullable;

import rx.functions.Action1;

/**
 * Created by Gavriil Sitnikov on 20/10/2015.
 * TODO: fill description
 */
@SuppressWarnings("PMD.AccessorClassGeneration")
public final class FrescoUtils {

    public static final BaseBitmapDataSubscriber EMPTY_CALLBACK = new BaseBitmapDataSubscriber() {

        @Override
        protected void onNewResultImpl(final Bitmap bitmap) {
            //do nothing
        }

        @Override
        protected void onFailureImpl(final DataSource<CloseableReference<CloseableImage>> dataSource) {
            //do nothing
        }

    };

    @NonNull
    public static Uri getResourceUri(final int resourceId) {
        return new Uri.Builder().scheme(UriUtil.LOCAL_RESOURCE_SCHEME).path(String.valueOf(resourceId)).build();
    }

    @NonNull
    public static ImageRequest loadAndHandleBitmapRequest(@NonNull final Uri imageUrl,
                                                          @NonNull final Action1<Bitmap> bitmapHandler) {
        return ImageRequestBuilder
                .newBuilderWithSource(imageUrl)
                .setPostprocessor(new RealCallback(bitmapHandler))
                .build();
    }

    private FrescoUtils() {
    }

    private static class RealCallback implements Postprocessor {

        private final Action1<Bitmap> bitmapHandler;

        private RealCallback(final Action1<Bitmap> bitmapHandler) {
            this.bitmapHandler = bitmapHandler;
        }

        @Override
        public CloseableReference<Bitmap> process(final Bitmap sourceBitmap, final PlatformBitmapFactory bitmapFactory) {
            final CloseableReference<Bitmap> result
                    = bitmapFactory.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight());
            bitmapHandler.call(result.get());
            return result;
        }

        @Override
        public String getName() {
            return null;
        }

        @Nullable
        @Override
        public CacheKey getPostprocessorCacheKey() {
            return null;
        }

    }

}
