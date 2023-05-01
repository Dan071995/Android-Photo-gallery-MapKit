package com.example.cameraroomrecyclerview.presentation

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.cameraroomrecyclerview.databinding.FragmentItemClickBlankBinding
import java.io.File


class ItemClickBlankFragment : Fragment() {

    private var _binding: FragmentItemClickBlankBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemClickBlankBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uri = Uri.parse(arguments?.getString("UriAsString_KEY"))
        val itemPosition = arguments?.getInt("ItemPosition_KEY")

        Glide.with(requireContext())
            .load(uri)
            .centerCrop()
            .into(binding.imageViewItemClickFragment)

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp() //Возвращаемся назад по стеку
        }

        binding.buttonDelete.setOnClickListener {
            viewModel.deleteFile(uri.toString()) //Удаляем файл с хранилища и из БД
            findNavController().navigateUp()    //Возвращаемся назад по стеку
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}