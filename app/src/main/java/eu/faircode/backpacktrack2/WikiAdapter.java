package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WikiAdapter extends ArrayAdapter<Wikipedia.Page> {
    private static final String TAG = "BPT2.Wiki";

    private Context context;
    private Location location;

    public WikiAdapter(Context context, List<Wikipedia.Page> pages, Location location) {
        super(context, 0, pages);
        this.context = context;
        this.location = location;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Wikipedia.Page page = getItem(position);
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.wiki, parent, false);

        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        TextView tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);
        TextView tvMeta = (TextView) convertView.findViewById(R.id.tvMeta);
        ImageView ivShare = (ImageView) convertView.findViewById(R.id.ivShare);

        tvTitle.setText(page.title);
        tvDistance.setText(Math.round(page.location.distanceTo(location)) + " m");
        tvMeta.setText(Uri.parse(page.baseurl).getHost() + (page.type == null ? "" : "/" + page.type));

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

