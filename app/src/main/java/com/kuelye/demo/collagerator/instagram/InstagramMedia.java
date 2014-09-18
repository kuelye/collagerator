package com.kuelye.demo.collagerator.instagram;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class InstagramMedia implements Parcelable {

    public static final Parcelable.Creator<InstagramMedia> CREATOR
            = new Parcelable.Creator<InstagramMedia>() {

        public InstagramMedia createFromParcel(Parcel in) {
            return new InstagramMedia(in);
        }

        public InstagramMedia[] newArray(int size) {
            return new InstagramMedia[size];
        }

    };

    private final String    mThumbnailImageUrl;
    private final String    mStandardResolutionImageUrl;
    private final int       mLikesCount;

    private boolean         mSelected;

    public InstagramMedia(String thumbnailImageUrl, String standardResolutionImageUrl, int likesCount) {
        mThumbnailImageUrl = thumbnailImageUrl;
        mStandardResolutionImageUrl = standardResolutionImageUrl;
        mLikesCount = likesCount;
        mSelected = false;
    }

    public InstagramMedia(Parcel in) {
        mThumbnailImageUrl = in.readString();
        mStandardResolutionImageUrl = in.readString();
        mLikesCount = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mThumbnailImageUrl);
        out.writeString(mStandardResolutionImageUrl);
        out.writeInt(mLikesCount);
    }

    public String getThumbnailImageUrl() {
        return mThumbnailImageUrl;
    }

    public String getStandardResolutionImageUrl() {
        return mStandardResolutionImageUrl;
    }

    public int getLikesCount() {
        return mLikesCount;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    public boolean isSelected() {
        return mSelected;
    }

    // -------------------- INNER --------------------

    public static class LikesCountDescendingComparator implements Comparator<InstagramMedia> {

        @Override
        public int compare(InstagramMedia oA, InstagramMedia oB) {
            return oA.getLikesCount() < oB.getLikesCount() ? 1 :
                    oA.getLikesCount() == oB.getLikesCount() ? 0 : -1;
        }

    }

}
