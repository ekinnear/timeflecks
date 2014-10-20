package core;

import java.util.ArrayList;
import java.util.Date;

/**
 * 
 * Represents a single scheduleable task -- the heart of the
 *         application.
 * 
 */

public class Task extends TimeObject
{
	private static final long serialVersionUID = 1L;
	
	private boolean completed;
	private Date dueDate;
	private int priority;
	private ArrayList<String> tags;

	public Task(String name)
	{
		super(name);
		completed = false;
	}

	/*
	 * A bunch of getters and setters, as well as a couple of additional
	 * functions.
	 */
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isCompleted()
	{
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public Date getDueDate()
	{
		return dueDate;
	}

	public void setDueDate(Date dueDate)
	{
		this.dueDate = dueDate;
	}

	public ArrayList<String> getTags()
	{
		return tags;
	}

	public void setTags(ArrayList<String> tags)
	{
		this.tags = tags;
	}

	public boolean hasTag(String tag)
	{
		return tags.contains(tag);
	}

	public void addTag(String tagname)
	{
		tags.add(tagname);
	}
}
