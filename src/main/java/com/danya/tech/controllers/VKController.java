package com.danya.tech.controllers;

import com.danya.tech.dao.FuckVkUserDao;
import com.danya.tech.entity.FuckVkUser;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import com.vk.api.sdk.objects.GroupAuthResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VKController {

    final RestTemplate restTemplate;

    final FuckVkUserDao fuckVkUserDao;

    @Value("${vk.groupId}")
    int groupId;

    @Value("${vk.clientId}")
    int clientId;

    @Value("${vk.clientSecret}")
    String clientSecret;

    @Value("${vk.redirectUri}")
    String redirectUri;


    @GetMapping("/vkAuth")
    public void vkAuth(@RequestParam("code") String code, @RequestParam("state") String state) throws ClientException, ApiException {
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);

        GroupAuthResponse groupAuthResponse = vk.oAuth()
                .groupAuthorizationCodeFlow(clientId, clientSecret, redirectUri, code)
                .execute();

        FuckVkUser fuckVkUser = new FuckVkUser(Long.valueOf(state), groupAuthResponse.getAccessTokens().get(groupId));
        fuckVkUserDao.saveFuckVkUser(fuckVkUser);
    }
}
