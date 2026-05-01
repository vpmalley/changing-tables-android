package fr.vpm.changingtables.ui.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import fr.vpm.changingtables.databinding.FragmentSavedBinding
import fr.vpm.changingtables.viewmodels.BusinessViewModel

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null

    private val businessViewModel: BusinessViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SavedBusinessesAdapter()
        binding.savedRecyclerView.adapter = adapter

        businessViewModel.savedBusinesses.observe(viewLifecycleOwner) { businesses ->
            adapter.submitList(businesses)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
