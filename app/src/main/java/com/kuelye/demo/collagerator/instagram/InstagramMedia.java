package com.kuelye.demo.collagerator.instagram;

public class InstagramMedia {

    private final String mImage;
    private final int mLikesCount;

    public InstagramMedia(String image, int likesCount) {
        mImage = image;
        mLikesCount = likesCount;
    }

    public String getImage() {
        return mImage;
    }

    public int getLikesCount() {
        return mLikesCount;
    }

    @Override
    public String toString() {
        return mImage + "," + mLikesCount;
    }

}
