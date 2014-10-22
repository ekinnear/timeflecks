package core;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

//I'm not sure if this should / shouldn't be a singleton...
public class TaskList
{
	static TaskList instance;
	
	public static TaskList getInstance()
	{
		if (instance == null)
		{
			instance = new TaskList();
		}
		return instance;
	}
	
	// Class implementation
	
	private ArrayList<Task> tasks;
	private ArrayList<Event> events;
	private transient Logger logger;
	
	private TaskList()
	{
		tasks = new ArrayList<Task>();
		events = new ArrayList<Event>();
	}

	public ArrayList<Task> getTasks()
	{
		return tasks;
	}

	public ArrayList<Event> getEvents()
	{
		return events;
	}
	
	public void setTasks(ArrayList<Task> tasks)
	{
		this.tasks = tasks;
	}
	
	public void setEvents(ArrayList<Event> events)
	{
		this.events = events;
	}
	
	public void addEvent(Event e) {
		logger.logp(Level.INFO, "core.TaskList", "core.TaskList.addEvent(e)",
				"Adding new event with id " + e.getId() + " to event list");
		events.add(e);
	}
	
	public void addTask(Task t) {
		logger.logp(Level.INFO, "core.TaskList", "core.TaskList.addTask(e)",
				"Adding new task with id " + t.getId() + " to task list");
		tasks.add(t);
	}
	
		
	/**
	 * Saves all tasks to the database.
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public void saveAllTasksAndEvents() throws SQLException, IOException {
		logger.logp(Level.INFO, "core.TaskList", 
				"core.TaskList.saveAllTasksAndEvents",
				"saving all tasks and events to database");
		for (Task t : tasks) {
			t.saveToDatabase();
		}
		
		for (Event e : events) {
			e.saveToDatabase();
		}
	}

}
