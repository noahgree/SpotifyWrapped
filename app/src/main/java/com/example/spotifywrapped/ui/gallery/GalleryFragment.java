package com.example.spotifywrapped.ui.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentGalleryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private RecyclerView recyclerView;
    //private WrapAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        MainActivity mainActivity = MainActivity.getInstance();

        recyclerView = root.findViewById(R.id.wrapRecycler); // make sure recyclerView is in your FragmentGalleryBinding
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //adapter = new WrapAdapter(new ArrayList<>()); // Assuming you have a method to get your data
        //recyclerView.setAdapter(adapter);
        //for ()

        // Set the click listener for the button
        binding.addButtonTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_addWrap);
            }
        });

//        final TextView textView = binding.textGallery;
//        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

/*public class WrapAdapter extends RecyclerView.Adapter<WrapAdapter.WrapViewHolder> {
    private List<Map<String, Object>> wrapList; // Adjust the data type according to your needs

    public static class WrapViewHolder extends RecyclerView.ViewHolder {
        // Public variables for each component in a row
        public TextView accountName, timeFrame, artistName, songName, number, classTimes;
        public ImageView imageView1, imageView2;

        public WrapViewHolder(View itemView) {
            super(itemView);
            accountName = itemView.findViewById(R.id.accountName);
            timeFrame = itemView.findViewById(R.id.timeFrame);
            artistName = itemView.findViewById(R.id.textView6);
            songName = itemView.findViewById(R.id.textView7);
            number = itemView.findViewById(R.id.number);
            classTimes = itemView.findViewById(R.id.classTimes);
            imageView1 = itemView.findViewById(R.id.imageView2);
            imageView2 = itemView.findViewById(R.id.imageView3);
        }
    }

    public WrapAdapter(List<Map<String, Object>> wraps) {
        this.wrapList = wraps;
    }

    @Override
    public WrapViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wrap_item_layout, parent, false);
        return new WrapViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WrapViewHolder holder, int position) {
        Map<String, Object> wrap = wrapList.get(position);
        // Set data to your holder views from the wrap object
        // Example: holder.accountName.setText(wrap.get("accountName").toString());
    }

    @Override
    public int getItemCount() {
        return wrapList.size();
    }
}*/
