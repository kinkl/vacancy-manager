package me.anonymoussoftware.vacancymanager;

import java.awt.EventQueue;

import javax.swing.JFrame;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import me.anonymoussoftware.vacancymanager.ui.frames.MainFrame;

public class App 
{
	private static final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
	
	public static <T> T getBean(Class<T> requiredType) {
		return ctx.getBean(requiredType);
	}
	
	public static void main(String[] args) {
		ctx.register(AppConfig.class);
		ctx.refresh();
		EventQueue.invokeLater(() -> {
			MainFrame mainFrame = new MainFrame();
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainFrame.setVisible(true);
		});
	}
	
}
