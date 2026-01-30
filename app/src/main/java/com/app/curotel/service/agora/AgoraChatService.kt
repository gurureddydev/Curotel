package com.app.curotel.service.agora

import android.content.Context
import android.util.Log
import com.app.curotel.domain.model.ChatMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.CallBack
import io.agora.MessageListener
import io.agora.chat.ChatClient
import io.agora.chat.ChatOptions
import io.agora.chat.ChatMessage as AgoraMessage
import io.agora.chat.TextMessageBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgoraChatService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private var messageListener: MessageListener? = null

    fun initialize() {
        val options = ChatOptions()
        // Use the dedicated Chat App Key
        if (com.app.curotel.service.agora.AgoraConfig.CHAT_APP_KEY != "YOUR_CHAT_APP_KEY") {
            options.appKey = com.app.curotel.service.agora.AgoraConfig.CHAT_APP_KEY
        } else {
             Log.e("AgoraChatService", "CHAT_APP_KEY is not set in AgoraConfig.kt!")
        }
        
        options.autoLogin = false
        Log.d("AgoraChatService", "Initializing Chat with Key: ${options.appKey}")
        ChatClient.getInstance().init(context, options)
        setupMessageListener()
    }

    fun login(username: String, token: String, onSuccess: () -> Unit, onError: (Int, String) -> Unit) {
        ChatClient.getInstance().loginWithAgoraToken(username, token, object : CallBack {
            override fun onSuccess() {
                Log.d("AgoraChatService", "Login success")
                onSuccess()
            }

            override fun onError(code: Int, error: String?) {
                Log.e("AgoraChatService", "Login failed: $code $error")
                onError(code, error ?: "Unknown error")
            }
        })
    }

    fun logout() {
        ChatClient.getInstance().logout(true)
    }

    fun isLoggedIn(): Boolean {
        return ChatClient.getInstance().isLoggedInBefore
    }

    fun getCurrentUser(): String? {
        return ChatClient.getInstance().currentUser
    }

    fun sendMessage(content: String, to: String) {
        val message = AgoraMessage.createTextSendMessage(content, to)
        message.setMessageStatusCallback(object : CallBack {
            override fun onSuccess() {
                Log.d("AgoraChatService", "Message sent success")
                addLocalMessage(message)
            }

            override fun onError(code: Int, error: String?) {
                Log.e("AgoraChatService", "Message sent failed: $code $error")
            }
        })
        ChatClient.getInstance().chatManager().sendMessage(message)
    }

    private fun setupMessageListener() {
        messageListener = object : MessageListener {
            override fun onMessageReceived(messages: List<AgoraMessage>?) {
                messages?.forEach { msg ->
                    if (msg.type == AgoraMessage.Type.TXT) {
                        val body = msg.body as TextMessageBody
                        val chatMessage = ChatMessage(
                            id = msg.msgId,
                            senderId = msg.from,
                            content = body.message,
                            timestamp = msg.msgTime,
                            isSelf = false
                        )
                        _messages.update { it + chatMessage }
                    }
                }
            }
            
            override fun onCmdMessageReceived(messages: List<AgoraMessage>?) {}
            override fun onMessageRead(messages: List<AgoraMessage>?) {}
            override fun onMessageDelivered(messages: List<AgoraMessage>?) {}
            override fun onMessageRecalled(messages: List<AgoraMessage>?) {}
            override fun onReactionChanged(messageList: List<io.agora.chat.MessageReactionChange>?) {}
        }
        
        ChatClient.getInstance().chatManager().addMessageListener(messageListener)
    }

    private fun addLocalMessage(agoraMessage: AgoraMessage) {
        val body = agoraMessage.body as TextMessageBody
        val chatMessage = ChatMessage(
            id = agoraMessage.msgId,
            senderId = agoraMessage.from,
            content = body.message,
            timestamp = agoraMessage.msgTime,
            isSelf = true
        )
        _messages.update { it + chatMessage }
    }

    fun onDestroy() {
        messageListener?.let {
            ChatClient.getInstance().chatManager().removeMessageListener(it)
        }
        logout()
    }
}
