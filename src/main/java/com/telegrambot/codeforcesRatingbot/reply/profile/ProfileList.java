package com.telegrambot.codeforcesRatingbot.reply.profile;

import com.telegrambot.codeforcesRatingbot.bot.BotState;
import com.telegrambot.codeforcesRatingbot.cache.UserCache;
import com.telegrambot.codeforcesRatingbot.model.Profile;
import com.telegrambot.codeforcesRatingbot.model.UserRatingSubscription;
import com.telegrambot.codeforcesRatingbot.reply.Reply;
import com.telegrambot.codeforcesRatingbot.sender.CommonMessages;
import com.telegrambot.codeforcesRatingbot.service.InfoRetrievingService;
import com.telegrambot.codeforcesRatingbot.service.UserRatingRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

@Service
public class ProfileList implements Reply {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserRatingRepositoryService subscriptionService;
    @Autowired
    InfoRetrievingService retrievingService;
    @Autowired
    CommonMessages messageService;
    @Autowired
    UserCache userCache;

    @Override
    public SendMessage sendMessage(Message message) {
        long chatId = message.getChatId();
        List<UserRatingSubscription> profileList = subscriptionService.findByChatId(chatId);
        if (profileList.size() == 0) {
            return messageService.sendMessage(chatId, "reply.list.profiles.noData");
        }
        StringBuilder stringBuilder = new StringBuilder();
        profileList.forEach(userRatingSubscription -> {
            String username = userRatingSubscription.getProfile();
            Profile profile = retrievingService.retrieveProfileByUsername(username);
            StringBuilder rank = new StringBuilder();
            Arrays.stream(profile.getRank().split(" ")).forEach(word -> rank.append(StringUtils.capitalize(word) + " "));
            rank.deleteCharAt(rank.length() - 1);
            String rating = "(" + Integer.toString(profile.getRating()) + ")";
            String handle = "[" + profile.getHandle() + "]" + "(https://codeforces.com/profile/" + profile.getHandle()+")";
            String line = String.format("%s %s %s", rank, rating, handle);
            stringBuilder.append(line + "\n");
        });
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return messageService.sendMessage(chatId, "reply.list.profiles", stringBuilder.toString());
    }

    @Override
    public BotState getReplyName() {
        return BotState.SHOW_SUBSCRIPTION_LIST;
    }
}
