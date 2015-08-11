package com.kylemsguy.tcasmobile.backend;

import android.graphics.Bitmap;

/**
 * Created by kyle on 10/08/15.
 */
public class ProfileManager {
    private static String PROFILE_URL = SessionManager.BASE_URL + "profile/";
    private static String PROFILE_IMG_URL = PROFILE_URL + "draw/update/";
    private SessionManager sm;

    public ProfileManager(SessionManager sessionManager) {
        sm = sessionManager;
    }

    public void updateProfileImage(Bitmap image) throws InvalidFileException {
        String imgData = new TCaSImageConverter(image).convertToTImg();


    }

    public Profile getProfile() {
        // TODO get profile and build Profile object
        return null;
    }

    public void updateProfile(Profile profile) {
        // TODO implement sending profile updates
    }

    public static class Profile {
        private String blurb;
        private String aim;
        private String msn;
        private String ymsg;
        private String gtalk;
        private String twitter;
        private String website;

        private Profile(String blurb, String aim, String msn, String ymsg, String gtalk, String twitter, String website) {
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
            private String blurb;
            private String aim;
            private String msn;
            private String ymsg;
            private String gtalk;
            private String twitter;
            private String website;

            public Builder() {

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
                return new Profile(blurb, aim, msn, ymsg, gtalk, twitter, website);
            }

        }
    }
}
