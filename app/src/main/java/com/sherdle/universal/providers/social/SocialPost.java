package com.sherdle.universal.providers.social;

import java.util.ArrayList;
import java.util.Date;

public class SocialPost {

    public enum PostSource { Twitter, Instagram, Facebook }
    public enum PostType { TEXT, IMAGE, VIDEO}

    public SocialPost(PostSource source){
        this.postSource = source;
    }

    public PostSource postSource;
    public String id;
    public PostType postType = PostType.TEXT;
    public String username;
    public String profilePhotoUrl;
    public String caption;
    public ArrayList<String> imageUrls = new ArrayList<>();
    public String videoUrl;
    public String link;
    public Date createdTime;
    public int likesCount;
    public int commentsOrRetweetCount;
    public String commentsJson;

}
