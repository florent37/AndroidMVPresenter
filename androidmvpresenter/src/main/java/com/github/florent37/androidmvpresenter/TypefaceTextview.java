/*
* Copyright 2013 Evgeny Shishkin
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.github.florent37.androidmvpresenter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import java.util.HashMap;

public class TypefaceTextview extends AppCompatTextView {

    private final static HashMap<String, Typeface> mTypefaces = new HashMap<>(16);

    public TypefaceTextview(Context context) {
        super(context);
    }

    public TypefaceTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context, attrs);
    }

    public TypefaceTextview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseAttributes(context, attrs);
    }

    private void parseAttributes(Context context, AttributeSet attrs) {
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.TypefaceTextview);
        String typefaceValue = values.getString(R.styleable.TypefaceTextview_typeface);
        values.recycle();

        if (typefaceValue != null) {
            setTypeface(obtainTypeface(context, typefaceValue));
        }
    }

    public void setTypeface(String typefaceValue) {
        setTypeface(obtainTypeface(getContext(), typefaceValue));
    }

    private Typeface obtainTypeface(Context context, String typefaceValue) throws IllegalArgumentException {
        Typeface typeface = mTypefaces.get(typefaceValue);
        if (typeface == null) {
            typeface = createTypeface(context, typefaceValue);
            mTypefaces.put(typefaceValue, typeface);
        }
        return typeface;
    }

    private Typeface createTypeface(Context context, String typefaceValue) throws IllegalArgumentException {
        return Typeface.createFromAsset(context.getAssets(), "fonts/" + typefaceValue);
    }

}