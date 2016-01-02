package com.kylemsguy.tcasmobile.backend;

import android.graphics.Bitmap;

import java.net.URLEncoder;

/**
 * Created by kyle on 10/08/15.
 * Manages the user's profile
 */
public class ProfileManager {
    private static final String PROFILE_URL = SessionManager.BASE_URL + "profile/";
    private static final String PROFILE_IMG_URL = PROFILE_URL + "draw/";
    private static final String PROFILE_IMG_UPDATE_URL = PROFILE_IMG_URL + "update/";

    private SessionManager sm;

    private Profile profile;

    public ProfileManager(SessionManager sessionManager) {
        sm = sessionManager;
    }

    /**
     * Update the user's Profile image with the bitmap given
     *
     * @param image The user's desired Profile image
     * @return The response from the server, usually the encoded image again.
     * @throws Exception
     */
    public String submitProfileImage(Bitmap image) throws Exception {
        //String imgData = new TCaSImageConverter(image).convertToTCaSImg();
        String imgData = new TCaSImageConverter(image).convertToTCaSUrlEncodedImg();
        String params = "data=" + URLEncoder.encode(imgData, "UTF-8");

        return sm.sendPost(PROFILE_IMG_UPDATE_URL, params);
    }

    /**
     * Gets a user's Profile image and converts it to a bitmap before returning it.
     *
     * @return
     */
    public Bitmap getProfileImage() throws Exception {
        String html = sm.getPageContent(PROFILE_IMG_URL);
        String imgData = TCaSImageConverter.extractImgData(html);
        return TCaSImageConverter.textToBitmap(imgData.trim());
    }

    public Profile getProfile() {
        if (profile == null) {
            // TODO get profile and build Profile object
        }
        return profile;
    }

    /**
     * Updates entire profile given a Profile object.
     * Please use other methods that only send a change in a single Profile element
     *
     * @param profile
     * @throws Exception
     */
    private void submitProfileUpdate(Profile profile) throws Exception {
        // TODO implement sending profile updates
    }

    public static class Profile {
        private Bitmap profileImage;
        private String blurb;
        private String aim;
        private String msn;
        private String ymsg;
        private String gtalk;
        private String twitter;
        private String website;

        private Profile(Bitmap profileImage, String blurb, String aim, String msn, String ymsg, String gtalk, String twitter, String website) {
            this.profileImage = profileImage;
            this.blurb = blurb;
            this.aim = aim;
            this.msn = msn;
            this.ymsg = ymsg;
            this.gtalk = gtalk;
            this.twitter = twitter;
            this.website = website;
        }

        public String getBlurb() {
            return blurb;
        }

        public void setBlurb(String blurb) {
            this.blurb = blurb;
        }

        public String getAim() {
            return aim;
        }

        public void setAim(String aim) {
            this.aim = aim;
        }

        public String getMsn() {
            return msn;
        }

        public void setMsn(String msn) {
            this.msn = msn;
        }

        public String getYmsg() {
            return ymsg;
        }

        public void setYmsg(String ymsg) {
            this.ymsg = ymsg;
        }

        public String getGtalk() {
            return gtalk;
        }

        public void setGtalk(String gtalk) {
            this.gtalk = gtalk;
        }

        public String getTwitter() {
            return twitter;
        }

        public void setTwitter(String twitter) {
            this.twitter = twitter;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public static class Builder {
            private String profileImageText;
            private String blurb;
            private String aim;
            private String msn;
            private String ymsg;
            private String gtalk;
            private String twitter;
            private String website;

            public Builder() {

            }

            public Builder setProfileImage(String profileImageText) {
                this.profileImageText = profileImageText;
                return this;
            }

            public Builder setBlurb(String blurb) {
                this.blurb = blurb;
                return this;
            }

            public Builder setAim(String aim) {
                this.aim = aim;
                return this;
            }

            public Builder setMsn(String msn) {
                this.msn = msn;
                return this;
            }

            public Builder setYmsg(String ymsg) {
                this.ymsg = ymsg;
                return this;
            }

            public Builder setGtalk(String gtalk) {
                this.gtalk = gtalk;
                return this;
            }

            public Builder setTwitter(String twitter) {
                this.twitter = twitter;
                return this;
            }

            public Builder setWebsite(String website) {
                this.website = website;
                return this;
            }

            public Profile build() {
                Bitmap profileImage = TCaSImageConverter.textToBitmap(profileImageText);
                return new Profile(profileImage, blurb, aim, msn, ymsg, gtalk, twitter, website);
            }

        }
    }
}
