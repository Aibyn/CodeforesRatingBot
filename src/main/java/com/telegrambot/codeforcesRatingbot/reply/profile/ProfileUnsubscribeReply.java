package com.telegrambot.codeforcesRatingbot.reply.profile;

import com.telegrambot.codeforcesRatingbot.bot.BotState;
import com.telegrambot.codeforcesRatingbot.cache.UserCache;
import com.telegrambot.codeforcesRatingbot.model.UserRatingSubscription;
import com.telegrambot.codeforcesRatingbot.reply.Reply;
import com.telegrambot.codeforcesRatingbot.sender.CommonMessages;
import com.telegrambot.codeforcesRatingbot.service.UserRatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileUnsubscribeReply implements Reply {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserRatingService subscriptionService;
    @Autowired
    CommonMessages messageService;
    @Autowired
    UserCache userCache;

    @Override
    public SendMessage sendMessage(Message message) {
        String profile = message.getText();
        long chatId = message.getChatId();
        int userId = message.getFrom().getId();
        // TODO(aibyn) Add Validtor for name
        List<UserRatingSubscription> profileList = subscriptionService.findByChatId(chatId);
        Optional<UserRatingSubscription> toDeleteOptional = profileList.stream().filter(arr -> profile.equals(arr.getProfile())).findFirst();
        userCache.setUserBotState(userId, BotState.NULL_STATE);
        if (!toDeleteOptional.isPresent()) {
            return messageService.sendMessage(chatId, "There is no such profile in your list");
        }
        UserRatingSubscription toDelete = toDeleteOptional.get();
        subscriptionService.deleteUserSubscription(toDelete);
        logger.info("User id = {} unsubscribed from -> {}", chatId, profile);
        return messageService.sendMessage(message.getChatId(), "Successfully unsubscribed");
    }

    @Override
    public BotState getReplyName() {
        return BotState.UNSUBSCRIPTION_PROCESS;
    }
}
