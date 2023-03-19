package com.seu.smartfarm.modules.glide;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.model.ModelCache;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;


@GlideModule
public class MAppGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, Registry registry) {
        registry.prepend(String.class, InputStream.class, new HeaderedLoader.Factory());
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    private static class HeaderedLoader extends BaseGlideUrlLoader {
        public static final Headers HEADERS = new LazyHeaders.Builder()
                .addHeader("User-Agent", "agent_test")
                .addHeader("Accept", "image/webp, */*")
                .build();

        protected HeaderedLoader(ModelLoader concreteLoader) {
            super(concreteLoader);
        }

        protected HeaderedLoader(ModelLoader concreteLoader, @Nullable ModelCache modelCache) {
            super(concreteLoader, modelCache);
        }

        @Override
        protected String getUrl(Object o, int width, int height, Options options) {
            return (String) o;
        }

        @Override
        public boolean handles(@NonNull Object o) {
            // will call [getUrl][getHeaders] if return true
            return true;
        }

        @Nullable
        @Override
        protected Headers getHeaders(Object o, int width, int height, Options options) {
            return HEADERS;
        }

        static class Factory implements ModelLoaderFactory<String, InputStream> {

            @Override
            public ModelLoader<String, InputStream> build(MultiModelLoaderFactory multiFactory) {
                ModelLoader<GlideUrl, InputStream> loader = multiFactory.build(GlideUrl.class, InputStream.class);
                return new HeaderedLoader(loader);
            }

            @Override
            public void teardown() { /* nothing to free */ }
        }
    }
}
