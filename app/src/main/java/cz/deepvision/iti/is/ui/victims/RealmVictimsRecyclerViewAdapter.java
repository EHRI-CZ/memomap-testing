package cz.deepvision.iti.is.ui.victims;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.Set;

import cz.deepvision.iti.is.R;
import cz.deepvision.iti.is.models.victims.Person;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

@Deprecated//("Zatím nebudeme používat realm")
public class RealmVictimsRecyclerViewAdapter extends RealmRecyclerViewAdapter<Person, RealmVictimsRecyclerViewAdapter.MyViewHolder> {
    private Set<Integer> countersToDelete = new HashSet<>();

    RealmVictimsRecyclerViewAdapter(OrderedRealmCollection<Person> data) {
        super(data, true);
        // Only set this if the model class has a primary key that is also a integer or long.
        // In that case, {@code getItemId(int)} must also be overridden to return the key.
        // See https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#hasStableIds()
        // See https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#getItemId(int)
        setHasStableIds(true);
    }

    Set<Integer> getCountersToDelete() {
        return countersToDelete;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Person obj = getItem(position);
        holder.data = obj;
        final long itemId = obj.getId();
        //noinspection ConstantConditions
        holder.title.setText(obj.getName());
    }

    @Override
    public long getItemId(int index) {
        //noinspection ConstantConditions
        return getItem(index).getId();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        public Person data;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.textview);
        }
    }
}
