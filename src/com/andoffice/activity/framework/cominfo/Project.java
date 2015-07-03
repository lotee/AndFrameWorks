package com.andoffice.activity.framework.cominfo;

import java.util.ArrayList;
import java.util.List;

import com.andframe.exception.AfToastException;

import android.view.View;
import android.widget.LinearLayout.LayoutParams;

public class Project {

	public static final int SELECTBAR = 0;
	public static final int TEXTBOX = 1;
	public static final int CUSTOM = 2;

	public int type = SELECTBAR;
	public String name = "";
	public View custom = null;
	public List<Item> items = new ArrayList<Item>();
	public LayoutParams mLayoutParams = null; 

	public Project() {
		// TODO Auto-generated constructor stub
	}

	public Project(String name) {
		// TODO Auto-generated constructor stub
		this.name = name;
	}

	public Project(String name, int type) {
		// TODO Auto-generated constructor stub
		this.type = type;
		this.name = name;
	}

	public Project(String name,View custom) {
		// TODO Auto-generated constructor stub
		this.type = CUSTOM;
		this.name = name;
		this.custom = custom;
	}

	Project(Project project) {
		// TODO Auto-generated constructor stub
		this.type = project.type;
		this.name = project.name;
		this.custom = project.custom;
		this.items = project.items;
		this.mLayoutParams = project.mLayoutParams; 
	}

	/**
	 * 检查内部 Item 是否全部为非空 
	 * @return true 全部非空 false 还有若干 为空
	 */
	public void doCheckNotnull()throws Exception{
		for (Item item : items) {
			//item.isEmpty() 的作用不只是检查空值，提交前必须执行 
			//所以放在 item.notnull 的前面
			if (item.isEmpty() && item.notnull) {
				throw new AfToastException("请输入完整信息："+item.name);
			}
		}
	}

	public void put(Item item) {
		// TODO Auto-generated method stub
		items.add(item);
		if(type == TEXTBOX //TEXTBOX 类型有效化
				&& item.type != Item.DISABLE
				&& item.type != Item.TEXTBOXMULTI
				&& item.type != Item.TEXTBOXSINGLE){
			item.type = Item.TEXTBOXMULTI;
		}
	}
}
