package ru.hladobor.vkparser;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by sever on 06.05.2017.
 */
public class VkConnectionAgent {

    public static URIBuilder buildFriendsURI(String userId, String token) {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("https").setHost("api.vk.com").setPath("/method/friends.get")
                //.setParameter("domain", GROUP_DOMAIN)
                .setParameter("user_id", userId)
                .setParameter("fields",
                        "nickname,domain,sex,bdate,city,country,timezone,has_mobile,contacts,education,"
                                + "relation, last_seen, status, can_write_private_message, " +
                                "can_see_all_posts, can_post, universities")
                .setParameter("count", "1000")
                .setParameter("v", "5.8")
                .setParameter("access_token", token);
        return uriBuilder;
    }

    public static URIBuilder builgGroupMembersURI(String groupId, String token, int count, int offset) {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("https").setHost("api.vk.com").setPath("/method/groups.getMembers")
                .setParameter("group_id", groupId)
                .setParameter("fields",
                        "bdate, city, country, domain, contacts, connections, site, education, universities, schools")
                .setParameter("offset", String.valueOf(offset))
                .setParameter("count", String.valueOf(count))
                .setParameter("v", "5.8")
                .setParameter("access_token", token);
        return uriBuilder;
    }

    public static URIBuilder buildGroupInfoURI(String groupId, String token) {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("https").setHost("api.vk.com").setPath("/method/groups.getById")
                .setParameter("group_ids", groupId)
                .setParameter("fields",
                        "city,country,place,description,members_count,activity,status,contacts,links")
                .setParameter("v", "5.8")
                .setParameter("access_token", token);
        return uriBuilder;
    }

    public static URIBuilder buildGetUserByIdURI(String userIds){
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("https").setHost("api.vk.com").setPath("/method/groups.getById")
                .setParameter("user_ids", userIds);
        return uriBuilder;
    }

    public static StringWriter getResponseContent(URIBuilder uriBuilder) {
        HttpResponse response = HttpConnectionAgent.connectResponse(uriBuilder);
        Integer status = response.getStatusLine().getStatusCode();
        if (status != 200) {
            return null;
        }
        StringWriter content = new StringWriter();
        try {
            IOUtils.copy(response.getEntity().getContent(), content);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return content;
    }

}
