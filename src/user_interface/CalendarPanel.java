package user_interface;

import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import javax.swing.*;
import javax.swing.TransferHandler.DropLocation;

import core.Task;
import core.Timeflecks;
import logging.GlobalLogger;

public class CalendarPanel extends JPanel implements MouseListener, MouseMotionListener
{
	private boolean drawTimes;
	private boolean drawRightSideLine;

	private Date date;
	private ArrayList<Task> tasksToPaint;
	private Task draggedTask;
	
	private DragSource ds;
	
	/**
	 * Auto generated default serial version UID
	 */
	private static final long serialVersionUID = 1L;

	public CalendarPanel(Date date, boolean drawTimes,
			boolean drawRightSideLine, int width, int height)
	{
		super();

		this.drawTimes = drawTimes;
		this.drawRightSideLine = drawRightSideLine;

		this.setDate(date);
		draggedTask = new Task("Temp");
		setTasksToPaint(new ArrayList<Task>());
		refresh();

		setBorder(BorderFactory.createEmptyBorder());
		// setBorder(BorderFactory.createLineBorder(Color.red));

		this.setPreferredSize(new Dimension(width, height));

		// TODO keep this from breaking if you make it too small (down in the
		// math)
		this.setMinimumSize(new Dimension(width, height));
		
		this.setTransferHandler(new CalendarPanelTransferHandler(this));
		this.setDropTarget(new DropTarget());
		addMouseListener(this);
		
		//ds = new DragSource();
		//ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
		//ds.addDragSourceListener(this);
		draggedTask = null;
		
		
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Dimension d = this.getSize();

		// Draw the times here
		if (drawTimes)
		{
			// We are going to draw from time = 12am - 11pm
			// 23 because we are adding 23 hours to this
			// 1 2 3 4 5 6 7 8 9 10 11 12 1 2 3 4 5 6 7 8 9 10 11
			// 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23

			int hr = 12;
			String ampm = "am";
			for (int insetFromTop = d.height / 24; insetFromTop < d.height; insetFromTop += d.height / 24)
			{
				// Draw a time at this height in from the top
				g.drawString("" + hr + ampm, 4, insetFromTop);

				if (hr == 12 && ampm.equals("am"))
				{
					hr = 1;
					ampm = "am";
				}
				else if (hr == 11 && ampm.equals("am"))
				{
					hr = 12;
					ampm = "pm";
				}
				else if (hr == 12 && ampm.equals("pm"))
				{
					hr = 1;
					ampm = "pm";
				}
				else if (hr == 11 && ampm.equals("pm"))
				{
					// We want to explicitly be done here
					break;
				}
				else
				{
					hr += 1;
				}
			}
		}

		// Draw the horizontal lines

		// ==================================================================
		// Change these to change how far in from the edges the lines are
		int insetFromLeft = 0; // See manual 10 later
		int insetFromRight = 8;
		// ==================================================================

		int leftInset = insetFromLeft;
		int rightInset = insetFromRight;

		if (drawTimes)
		{
			insetFromLeft += 8;
			// Our longest string is any two digit pm/am string, here we will
			// use 12pm
			leftInset = g.getFontMetrics(g.getFont()).stringWidth("12pm")
					+ insetFromLeft;
		}

		// Get the height of the font to draw the lines
		Graphics2D g2 = (Graphics2D) g;
		FontRenderContext frc = g2.getFontRenderContext();
		int fontHeight = (int) g2.getFont().getLineMetrics("12pm", frc)
				.getHeight();

		if (drawRightSideLine)
		{
			rightInset = 0 + insetFromRight;
		}

		for (int insetFromTop = d.height / 24 - (fontHeight / 2); insetFromTop < d.height
				- (fontHeight / 2); insetFromTop += d.height / 24)
		{
			g.drawLine(leftInset, insetFromTop, d.width - rightInset,
					insetFromTop);
		}

		// Now we have the lines and we have the times

		// --------------------------------------------------------------------------------
		// Draw the date at the top

		String dayOfWeek = new SimpleDateFormat("EEEE").format(this.getDate());
		String date = new SimpleDateFormat("MM/dd/yyyy").format(this.getDate());

		g.drawString(dayOfWeek, 2, fontHeight);
		g.drawString(date, 2, 2 * fontHeight);

		// ---------------------------------------------------------------------------------

		// We also have the option to display a line on the right hand side
		if (drawRightSideLine)
		{
			g.drawLine(d.width - 4, d.height / 24 / 2, d.width - 4, d.height
					- d.height / 24 / 2);
		}
		
		int firstInset = d.height / 24 - (fontHeight / 2);
		int hourIncrement = d.height / 24;
		Calendar calendar;
		double taskHours, durationInHours;
		Rectangle frame;
		// Go through and draw any tasks at the appropriate place
		for (Task t : tasksToPaint)
		{
			calendar = Calendar.getInstance();
			calendar.setTime(t.getStartTime());
			taskHours = (double) calendar.get(Calendar.HOUR_OF_DAY)
					+ (double) calendar.get(Calendar.MINUTE) / 60.0;

			durationInHours = ( t.getDuration() / 1000.0 / 60.0 / 60.0 ) % 24.0;

			frame = new Rectangle(leftInset, firstInset
					+ (int) (taskHours * hourIncrement), d.width - rightInset
					- leftInset, (int) (durationInHours * hourIncrement));
			
			TaskComponent tc = new TaskComponent(t, frame);
			add(tc);
			tc.addMouseListener(this);
			tc.addMouseMotionListener(this);
			tc.paint(g);
		}
		
		
		/*DropLocation loc= this.getDropLocation();
		this.getTransferHandler().
		if (loc == null) {
			return;
		}
		renderPrettyIndicatorAt(loc);*/
	}
	
	/**
	 * refreshes the calendar view, re-adds the tasks for that day from the
	 * TaskList. Warning: This may be time-intensive for large TaskLists
	 */
	public void refresh()
	{
		GlobalLogger.getLogger().logp(Level.INFO, "CalendarPanel", "refresh",
				"Refresh called.");

		// We need to go through the task list and update all tasks that match
		// the current date

		// Clear it out first
		tasksToPaint.clear();

		for (Task t : Timeflecks.getSharedApplication().getTaskList()
				.getTasks())
		{
			if (t.isScheduled())
			{
				// Same day will just return false if they are null
				if (sameDay(t.getStartTime(), this.getDate()))
				{
					// They are on the same day
					tasksToPaint.add(t);
				}
			}
		}
		
		
		// Now we have the list of tasks to paint, we want to make sure that
		// they are painted in paintComponent.

		// this.invalidate();
		this.repaint();
		// this.revalidate();
	}

	public boolean sameDay(Date firstDate, Date secondDate)
	{
		if (firstDate == null || secondDate == null)
		{
			return false;
		}

		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();

		c1.setTime(firstDate);
		c2.setTime(secondDate);

		boolean same = c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
				&& c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);

		return same;
	}

	public static void main(String[] args)
	{
		try
		{
			JFrame newFrame = new JFrame("Calendar Test");
			JPanel container = new JPanel();

			FlowLayout panelLayout = new FlowLayout();
			panelLayout.setHgap(0);
			panelLayout.setVgap(0);
			container.setLayout(panelLayout);

			JScrollPane s = new JScrollPane(container);

			int width = 150;
			int height = 1000;

			for (int i = 0; i < 7; i++)
			{
				CalendarPanel p;
				if (i == 0)
				{
					p = new CalendarPanel(new Date(), true, true, width, height);
				}
				else if (i == 6)
				{
					p = new CalendarPanel(new Date(), false, false, width,
							height);
				}
				else
				{
					p = new CalendarPanel(new Date(), false, true, width,
							height);
				}

				container.add(p);

			}

			// container.setSize(400,400);

			newFrame.getContentPane().add(s);

			newFrame.pack();

			newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			newFrame.setSize(400, 400);
			newFrame.setAutoRequestFocus(true);
			newFrame.setResizable(true);

			newFrame.setVisible(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Unable to " + e);
		}
	}

	public ArrayList<Task> getTasksToPaint()
	{
		return tasksToPaint;
	}

	public void setTasksToPaint(ArrayList<Task> tasksToPaint)
	{
		this.tasksToPaint = tasksToPaint;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}
	
	public Date getTime(Point p){
		Dimension d = this.getSize();
		System.out.println("Dim:"+d.toString());
		//location of first inset
		Date d1 = (Date) date.clone();
		int hourBlock = d.height / 24;
		int insetFromTop = d.height / 24;
		d1.setHours((p.y - insetFromTop) / hourBlock);
		d1.setMinutes(((p.y - insetFromTop) % hourBlock) * 60 / hourBlock);
		d1.setSeconds(0);
		
		return d1;
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2 && e.getSource() instanceof TaskComponent){
			TaskComponent c = (TaskComponent)e.getSource();
			NewTaskPanel p = new NewTaskPanel(c.getTask());
			p.displayFrame();
		}
		// TODO Auto-generated method stub
		//System.out.println(e.getPoint().toString());
		//Date d = getTime(e.getPoint());
		//System.out.println(d.toString());
		//System.out.println(e.getComponent().getClass().getCanonicalName());
		//System.out.println(e.getSource().getClass().getCanonicalName());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		//System.out.println("ENTERED: " + e.getSource().getClass().getCanonicalName());
		// TODO Auto-generated method stub
		//System.out.println("Entered!");
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println("Exited!");
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//.out.println("PRESSED: " + e.getSource().getClass().getCanonicalName());
		
		
		// TODO Auto-generated method stub
		//System.out.println("Pressed!");
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		//if (e.getComponent() instanceof TaskComponent){
			//this.getTransferHandler().exportAsDrag((JComponent)e.getComponent(), (InputEvent)e, DnDConstants.ACTION_MOVE);
			//ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
		//}
	}
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if (arg0.getSource() instanceof TaskComponent){
			System.out.println("Starting a mofo drag!!");
			this.getTransferHandler().exportAsDrag((JComponent)arg0.getSource(), arg0, DnDConstants.ACTION_MOVE);
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	@Override
	public void dragGestureRecognized(DragGestureEvent arg0) {
		// TODO Auto-generated method stub
		
		//ds.startDrag(arg0, DragSource.DefaultMoveDrop, this, this);
		//CalendarPanel parent = (CalendarPanel)this.getParent();
		//MouseListener lis = new MouseListener;
		//parent.getTransferHandler().ex
		//parent.getTransferHandler().exportAsDrag(this, , DnDConstants.ACTION_MOVE);
		//System.out.println("PARENT" +this.getParent().getClass().getCanonicalName());
		System.out.println("Drag noticed");
		///System.out.println("Origin: "+arg0.getDragOrigin().toString());

		//System.out.println(MouseInfo.getPointerInfo().getLocation().toString());
		//pressed = true;
		//entered = true;
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dragEnter(DragSourceDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dragExit(DragSourceEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dragOver(DragSourceDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dropActionChanged(DragSourceDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}*/

	
}