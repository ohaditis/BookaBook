package com.example.bookabook.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookabook.GeminiCallback;
import com.example.bookabook.GeminiChatManager;
import com.example.bookabook.GeminiPrompts;
import com.example.bookabook.R;
import com.example.bookabook.models.ChatMessage;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class UserGeminiFragment extends Fragment {

    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    
    private TextInputEditText etGeminiPrompt;
    private Button btnSendGemini;
    private ProgressBar progressGemini;

    private GeminiChatManager geminiChatManager;

    public UserGeminiFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_gemini, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvChat = view.findViewById(R.id.rvChat);
        etGeminiPrompt = view.findViewById(R.id.etGeminiPrompt);
        btnSendGemini = view.findViewById(R.id.btnSendGemini);
        progressGemini = view.findViewById(R.id.progressGemini);

        chatMessages = new ArrayList<>();
        // Add default message
        chatMessages.add(new ChatMessage("Hello! Tell me what books you like, and I will help you find your next read.", false));
        
        chatAdapter = new ChatAdapter(chatMessages);
        rvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChat.setAdapter(chatAdapter);

        geminiChatManager = GeminiChatManager.getInstance(GeminiPrompts.BOOK_ASSISTANT_PROMPT);

        btnSendGemini.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String userPrompt = etGeminiPrompt.getText().toString().trim();

        if (TextUtils.isEmpty(userPrompt)) {
            etGeminiPrompt.setError("Please enter a question");
            return;
        }

        // Add user message to list
        chatMessages.add(new ChatMessage(userPrompt, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        rvChat.scrollToPosition(chatMessages.size() - 1);
        
        etGeminiPrompt.setText("");
        setLoading(true);

        geminiChatManager.sendChatMessage(userPrompt, new GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    setLoading(false);
                    chatMessages.add(new ChatMessage(response, false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    rvChat.scrollToPosition(chatMessages.size() - 1);
                });
            }

            @Override
            public void onFailure(Throwable throwable) {
                if (!isAdded()) return;

                // Log the full error to Logcat for debugging
                Log.e("GeminiDebug", "Request failed", throwable);

                requireActivity().runOnUiThread(() -> {
                    setLoading(false);
                    String errorMsg = throwable.getMessage();
                    Toast.makeText(requireContext(),
                            "Gemini error: " + (errorMsg != null ? errorMsg : "Unknown error"),
                            Toast.LENGTH_LONG).show();
                    
                    chatMessages.add(new ChatMessage("Sorry, I could not get a response. Please try again.", false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    rvChat.scrollToPosition(chatMessages.size() - 1);
                });
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressGemini.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSendGemini.setEnabled(!isLoading);
        etGeminiPrompt.setEnabled(!isLoading);
    }
}