package eu.faircode.backpacktrack2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeocoderEx {
    private Geocoder geocoder;

    private static final String TAG = "BPT2.Geocoder";

    public static boolean isPresent() {
        return Geocoder.isPresent();
    }

    public GeocoderEx(Context context) {
        this.geocoder = new Geocoder(context, Locale.getDefault());
    }

    public String reverseGeocode(Location location) {
        if (location == null)
            return null;
        try {
            List<AddressEx> listAddress = getFromLocation(location, 1);
            return (listAddress.size() == 0 ? null : listAddress.get(0).name);
        } catch (IOException ex) {
            Log.w(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            return null;
        }
    }

    public List<AddressEx> getFromLocation(Location location, int results) throws IOException {
        return getAddressList(this.geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), results));
    }

    public List<AddressEx> getFromLocationName(String name, int results) throws IOException {
        return getAddressList(this.geocoder.getFromLocationName(name, results));
    }

    private static List<AddressEx> getAddressList(List<Address> listAddress) {
        List<AddressEx> listAddressEx = new ArrayList<>();
        if (listAddress != null)
            for (Address address : listAddress)
                if (address.hasLatitude() && address.hasLongitude()) {
                    List<String> listLine = new ArrayList<>();
                    for (int l = 0; l < address.getMaxAddressLineIndex(); l++)
                        listLine.add(address.getAddressLine(l));
                    AddressEx addressEx = new AddressEx();
                    addressEx.name = TextUtils.join(", ", listLine);
                    addressEx.location = new Location("geocoded");
                    addressEx.location.setLatitude(address.getLatitude());
                    addressEx.location.setLongitude(address.getLongitude());
                    addressEx.location.setTime(System.currentTimeMillis());
                    listAddressEx.add(addressEx);
                }
        return listAddressEx;
    }

    public static class AddressEx {
        public String name;
        public Location location;
    }

    public static CharSequence[] getNameList(List<AddressEx> listAddress) {
        List<CharSequence> listResult = new ArrayList<CharSequence>();
        for (AddressEx address : listAddress)
            listResult.add(address.name);
        return listResult.toArray(new CharSequence[0]);
    }
}