package com.danya.tech.bot;

import com.danya.tech.repositories.FuckVkUserRepository;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.ConversationWithMessage;
import com.vk.api.sdk.objects.messages.Keyboard;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.responses.GetConversationsResponse;
import com.vk.api.sdk.objects.messages.responses.GetHistoryResponse;
import com.vk.api.sdk.objects.users.Fields;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)

public class FuckVkBot extends TelegramLongPollingBot {

    final FuckVkUserRepository fuckVkUserRepository;

    @Value("${vk.groupId}")
    int groupId;

    @Value("${vk.authLink}")
    String authLink;

    final String START = "/start";

    final String AUTH = "Войти в вк";

    final String GET_UNREAD_MESSAGES = "Непрочитанные сообщения";

    public FuckVkBot(@Value("${telegram.token}") String botToken, @Autowired FuckVkUserRepository fuckVkUserRepository) {
        super(botToken);
        this.fuckVkUserRepository = fuckVkUserRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        var message = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();

        switch (message) {
            case START -> sendMessage(chatId, "Привет, войди в вк аккаунт через этого бота, чтобы получить доступ");

            case AUTH -> sendMessage(chatId, authLink + chatId);

            case GET_UNREAD_MESSAGES -> sendLastMessages(chatId);
        }

    }

    @Override
    public String getBotUsername() {
        return "fuckVkBot";
    }


    @SneakyThrows
    public void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);

        sendMessage.setReplyMarkup(getMainMenuKeyBoard());
        execute(sendMessage);
    }

    @SneakyThrows
    public void sendLastMessages(Long chatId) {
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);

        GroupActor actor = new GroupActor(groupId, fuckVkUserRepository.findByChatId(chatId).get().getAuthToken());

        GetConversationsResponse getConversationsResponse = vk.messages().getConversations(actor)
                .execute();

        List<ConversationWithMessage> messageList = getConversationsResponse.getItems()
                .stream()
                .filter(message -> message.getConversation().getUnreadCount() != null)
                .collect(Collectors.toList());

        Map<String, Integer> senderVsUnReadCount = new HashMap<>();

        messageList.stream()
                .forEach(message -> senderVsUnReadCount.put(message.getLastMessage().getFromId().toString(),message.getConversation().getUnreadCount()));

        Map<String, List<Message>> userIdVsHistoryMessages = new HashMap<>();

        for (String userId : senderVsUnReadCount.keySet()) {
            GetHistoryResponse response = vk.messages()
                    .getHistory(actor)
                    .count(senderVsUnReadCount.get(userId))
                    .userId(Integer.valueOf(userId))
                    .execute();
            userIdVsHistoryMessages.put(userId, response.getItems());
        }

        StringBuilder ids = new StringBuilder();
        userIdVsHistoryMessages.keySet()
                .stream()
                .forEach(user -> ids.append(user + ","));
        ids.deleteCharAt(ids.length() - 1);

        List<GetResponse> users = vk.users().get(actor)
                .userIds(ids.toString())
                .fields(Fields.DOMAIN)
                .execute();

        for (GetResponse user : users) {

            StringBuilder text = new StringBuilder();
            text.append("https://vk.com/")
                    .append(user.getDomain())
                    .append("\n")
                    .append("От: ")
                    .append(user.getFirstName())
                    .append(" ")
                    .append(user.getLastName())
                    .append("\n");

            userIdVsHistoryMessages.get(user.getId().toString())
                    .sort(Comparator.comparingInt(Message::getDate));
            userIdVsHistoryMessages.get(user.getId().toString())
                    .forEach(message -> text.append(formatDate(message.getDate())).append(": ").append(message.getText()).append("\n"));

            sendMessage(chatId, text.toString());
        }
    }

    @SneakyThrows
    private String formatDate(Integer unixEpochDate) {
        Long longVal = Long.valueOf(unixEpochDate);
        LocalDateTime localDate = Instant.ofEpochSecond(longVal).atZone(ZoneId.systemDefault()).toLocalDateTime();
        SimpleDateFormat inDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date date = inDateFormat.parse(localDate.toString());
        SimpleDateFormat outDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return outDateFormat.format(date);
    }

    private InlineKeyboardMarkup getInlineMessageButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton authButton = new InlineKeyboardButton();
        InlineKeyboardButton getUnreadMessagesButton = new InlineKeyboardButton();

        authButton.setText("Войти в вк");
        authButton.setCallbackData(AUTH);
        getUnreadMessagesButton.setText("Непрочитанные сообщения");
        getUnreadMessagesButton.setCallbackData(GET_UNREAD_MESSAGES);

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(authButton);
        row1.add(getUnreadMessagesButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row1);

        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }


    private ReplyKeyboardMarkup getMainMenuKeyBoard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();

        KeyboardButton authButton = new KeyboardButton("Войти в вк");
        KeyboardButton unreadButton = new KeyboardButton("Непрочитанные сообщения");
        row1.add(authButton);
        row2.add(unreadButton);

        keyboardRows.add(row1);
        keyboardRows.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboardRows);

        return replyKeyboardMarkup;
    }
}
