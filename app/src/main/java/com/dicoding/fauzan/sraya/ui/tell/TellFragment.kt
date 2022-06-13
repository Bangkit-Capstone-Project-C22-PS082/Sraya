package com.dicoding.fauzan.sraya.ui.tell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.fauzan.sraya.*
import com.dicoding.fauzan.sraya.databinding.FragmentTellBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class TellFragment : Fragment() {

    private var _binding: FragmentTellBinding? = null
    private val binding get() = _binding!!
    private lateinit var messagingAdapter: MessagingAdapter
    private val messagesList = mutableListOf<Message>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentTellBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.rvTellMessage.apply {
            messagingAdapter = MessagingAdapter()
            adapter = messagingAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }

        binding.btnTellSend.setOnClickListener {
            val message = binding.edtTellChat.text.toString()
            val timeFormat = Time.getTimeFormat()
            messagesList.add(
                Message(
                    Constants.SEND_ID, message,
                    timeFormat
                )
            )
            messagingAdapter.submitList(messagesList)
            binding.rvTellMessage.scrollToPosition(messagingAdapter.itemCount - 1)
            binding.edtTellChat.text?.clear()
            messagesList.add(
                Message(
                    Constants.RECEIVE_ID, BotResponses.reply(message),
                    timeFormat
                )
            )
            binding.rvTellMessage.scrollToPosition(messagingAdapter.itemCount - 1)

        }
        // For initializing message
        messagesList.add(Message(
            Constants.RECEIVE_ID,
            "Hai. Apa ada yang bisa saya bantu",
            Time.getTimeFormat()))
        messagingAdapter.submitList(messagesList)
        binding.rvTellMessage.scrollToPosition(messagingAdapter.itemCount - 1)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}