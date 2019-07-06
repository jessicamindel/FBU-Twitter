package com.codepath.apps.twitter.models;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    public String name;
    public long uid;
    public String screenName, profileImageUrl, bannerImageUrl;

    public static User fromJSON(JSONObject obj) throws JSONException {
        User u = new User();
        u.name = obj.getString("name");
        u.uid = obj.getLong("id");
        u.screenName = obj.getString("screen_name");
        u.profileImageUrl = obj.getString("profile_image_url_https");
        try {
            u.bannerImageUrl = obj.getString("profile_banner_url");
        } catch (JSONException e) {
            u.bannerImageUrl = null;
        }
        return u;
    }

    public boolean hasBannerImage() {
        return bannerImageUrl != null;
    }
}
