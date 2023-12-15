package com.example.steganography

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.steganography.databinding.FragmentListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() =_binding!!

    private lateinit var recyclerView:RecyclerView
    private lateinit var messageList:ArrayList<MessageClass>
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater,container,false)


        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)

        messageList = arrayListOf()

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        db = FirebaseFirestore.getInstance()

        db.collection(userId).get().addOnSuccessListener {
            if(!it.isEmpty)
            {
                for(data in it.documents)
                {
                    val message:MessageClass? = data.toObject(MessageClass::class.java)
                    if (message != null) {
                        messageList.add(message)
                    }
                }
                recyclerView.adapter = MyAdapter(messageList)
            }
        }
            .addOnFailureListener {
                Toast.makeText(activity,it.toString(),Toast.LENGTH_SHORT).show()
            }


        return binding.root
    }

}