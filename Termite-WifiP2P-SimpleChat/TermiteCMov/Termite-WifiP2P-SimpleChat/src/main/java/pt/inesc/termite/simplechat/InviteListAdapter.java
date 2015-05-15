package pt.inesc.termite.simplechat;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class InviteListAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<String> list = new ArrayList<String>();
    private ArrayList<String> deleteList = new ArrayList<String>();

    private Context context;
    private  ListView listViewDelete;
    private View.OnClickListener  ocl;


    public InviteListAdapter(ArrayList<String> list, Context context) {
        this.list = list;
        this.context = context;
    }


    public void setDeletOnClickListener(View.OnClickListener ocl){
        this.ocl = ocl;
    }

    public ArrayList<String> getDeleteList(){
        return deleteList;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.view_invite_list, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position));

        listViewDelete= (ListView) view.findViewById(R.id.listViewDelete);

        //Handle buttons and add onClickListeners
        Button deleteBtn = (Button)view.findViewById(R.id.delete_btn);


        deleteBtn.setOnClickListener(ocl);
        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                deleteList.add(list.get(position));



                list.remove(position);
                notifyDataSetChanged();
            }
        });

        return view;
    }
}