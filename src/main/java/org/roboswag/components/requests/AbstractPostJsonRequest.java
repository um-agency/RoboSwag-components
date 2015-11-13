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

package org.roboswag.components.requests;

import android.support.annotation.NonNull;

import com.google.api.client.http.AbstractHttpContent;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Post request that returns data in JSON format
 */
public abstract class AbstractPostJsonRequest<T> extends AbstractJsonRequest<T> {

    @NonNull
    protected abstract AbstractHttpContent getContent();

    protected AbstractPostJsonRequest(@NonNull final Class<T> responseResultType) {
        super(responseResultType);
    }

    @NonNull
    @Override
    protected Request.Builder createHttpRequest() throws IOException {
        final AbstractHttpContent content = getContent();
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        getContent().writeTo(byteArrayOutputStream);
        return super.createHttpRequest().post(RequestBody.create(
                MediaType.parse(content.getMediaType().build()), byteArrayOutputStream.toByteArray()));
    }

}
