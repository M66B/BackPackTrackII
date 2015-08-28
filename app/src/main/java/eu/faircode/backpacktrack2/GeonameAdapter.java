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

public class GeonameAdapter extends ArrayAdapter<Geonames.Geoname> {
    private static final String TAG = "BPT2.Geonames";

    private Context context;
    private Location location;

    public GeonameAdapter(Context context, List<Geonames.Geoname> names, Location location) {
        super(context, 0, names);
        this.context = context;
        this.location = location;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Geonames.Geoname name = getItem(position);
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.geoname, parent, false);

        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        TextView tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);
        TextView tvType = (TextView) convertView.findViewById(R.id.tvType);

        tvTitle.setText(name.name);
        tvDistance.setText(Math.round(name.location.distanceTo(location)) + " m");
        tvType.setText(
                (name.fcodeName.length() > 20 ? name.fcodeName.substring(0, 20) + " …" : name.fcodeName) +
                        (name.population == 0 ? "" : " " + name.population));

        return convertView;
    }
}

