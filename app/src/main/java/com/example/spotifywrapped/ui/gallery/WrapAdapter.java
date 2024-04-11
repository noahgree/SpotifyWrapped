/*import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifywrapped.R;

import java.util.List;
import java.util.Map;

public class WrapAdapter extends RecyclerView.Adapter<WrapAdapter.WrapViewHolder> {
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
