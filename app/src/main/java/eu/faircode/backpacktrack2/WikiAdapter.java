package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class WikiAdapter extends ArrayAdapter<Wikimedia.Page> {
    private static final String TAG = "BPT2.Wiki";

    private Context context;
    private Location location;

    public WikiAdapter(Context context, List<Wikimedia.Page> pages, Location location) {
        super(context, 0, pages);
        this.context = context;
        this.location = location;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Wikimedia.Page page = getItem(position);
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.wiki, parent, false);

        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        TextView tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);
        TextView tvSource = (TextView) convertView.findViewById(R.id.tvSource);
        ImageView ivShare = (ImageView) convertView.findViewById(R.id.ivShare);

        tvTitle.setText(page.title);
        tvDistance.setText(Math.round(page.location.distanceTo(location)) + " m");
        tvSource.setText(Uri.parse(page.baseurl).getHost() + (page.type == null ? "" : "/" + page.type));

        ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String uri = "geo:" + page.location.getLatitude() + "," + page.location.getLongitude() +
                            "?q=" + page.location.getLatitude() + "," + page.location.getLongitude() + "(" + Uri.encode(page.title) + ")";
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                } catch (Throwable ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                    Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return convertView;
    }
}

