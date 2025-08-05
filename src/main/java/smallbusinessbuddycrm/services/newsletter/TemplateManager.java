package smallbusinessbuddycrm.services.newsletter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Manages newsletter templates and provides template content
 */
public class TemplateManager {

    private static final Map<String, String> TEMPLATES = new HashMap<>();

    static {
        initializeTemplates();
    }

    private static void initializeTemplates() {
        TEMPLATES.put("Modern Clean", """
            <div class="welcome-section">
                <h2 style="color: #333; margin-bottom: 15px;">Welcome to Our Newsletter</h2>
                <p>Thank you for subscribing! Here's what's new this month.</p>
            </div>
            
            <div class="content-section">
                <h3 style="color: #007bff; margin: 25px 0 10px 0;">Latest Updates</h3>
                <p>Share your latest news, updates, or announcements here.</p>
                
                <h3 style="color: #007bff; margin: 25px 0 10px 0;">Featured Content</h3>
                <p>Highlight your most important content or featured products.</p>
            </div>
            
            <div class="cta-section" style="text-align: center; margin: 30px 0;">
                <a href="#" style="background-color: #007bff; color: white; padding: 12px 24px; 
                   text-decoration: none; border-radius: 5px; display: inline-block;">
                    Learn More
                </a>
            </div>
            """);

        TEMPLATES.put("Professional Blue", """
            <div style="background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
                <h2 style="color: #0066cc; margin: 0;">Professional Update</h2>
                <p style="margin: 10px 0 0 0; color: #666;">Your trusted business partner</p>
            </div>
            
            <div class="announcement">
                <h3 style="color: #333;">Important Announcement</h3>
                <p>Share important business updates or announcements with your audience.</p>
            </div>
            
            <div class="services" style="margin: 25px 0;">
                <h3 style="color: #0066cc;">Our Services</h3>
                <ul style="line-height: 1.8;">
                    <li>Service 1 - Brief description</li>
                    <li>Service 2 - Brief description</li>
                    <li>Service 3 - Brief description</li>
                </ul>
            </div>
            """);

        TEMPLATES.put("Warm Welcome", """
            <div style="text-align: center; margin-bottom: 30px;">
                <h1 style="color: #e74c3c; font-size: 28px;">Welcome!</h1>
                <p style="font-size: 18px; color: #7f8c8d;">We're excited to have you join our community</p>
            </div>
            
            <div class="intro">
                <p>Dear Friend,</p>
                <p>Thank you for joining us! We're thrilled to welcome you to our community of 
                   subscribers who love what we do.</p>
            </div>
            
            <div class="what-to-expect" style="background: #fff5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
                <h3 style="color: #e74c3c;">What to Expect</h3>
                <ul>
                    <li>Weekly updates and tips</li>
                    <li>Exclusive content just for subscribers</li>
                    <li>Special offers and early access</li>
                </ul>
            </div>
            """);

        TEMPLATES.put("Product Update", """
            <div class="product-header" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                 color: white; padding: 25px; border-radius: 8px; text-align: center; margin-bottom: 25px;">
                <h1 style="margin: 0; font-size: 24px;">Product Update</h1>
                <p style="margin: 10px 0 0 0; opacity: 0.9;">New features and improvements</p>
            </div>
            
            <div class="new-features">
                <h3 style="color: #667eea;">🚀 What's New</h3>
                <div style="background: #f8f9fa; padding: 15px; border-radius: 6px; margin: 15px 0;">
                    <h4 style="margin: 0 0 10px 0; color: #333;">Feature Name</h4>
                    <p style="margin: 0; color: #666;">Brief description of the new feature and how it benefits users.</p>
                </div>
            </div>
            
            <div class="improvements">
                <h3 style="color: #667eea;">✨ Improvements</h3>
                <ul>
                    <li>Performance improvements</li>
                    <li>Bug fixes and stability</li>
                    <li>Enhanced user experience</li>
                </ul>
            </div>
            """);

        TEMPLATES.put("Event Announcement", """
            <div class="event-banner" style="background: #ff6b6b; color: white; padding: 30px; 
                 text-align: center; border-radius: 10px; margin-bottom: 25px;">
                <h1 style="margin: 0; font-size: 26px;">📅 Upcoming Event</h1>
                <p style="margin: 10px 0 0 0; font-size: 16px;">Don't miss out on this special occasion!</p>
            </div>
            
            <div class="event-details">
                <h3 style="color: #ff6b6b;">Event Details</h3>
                <div style="background: #fff5f5; padding: 20px; border-radius: 8px;">
                    <p><strong>📍 Location:</strong> [Event Location]</p>
                    <p><strong>📅 Date:</strong> [Event Date]</p>
                    <p><strong>⏰ Time:</strong> [Event Time]</p>
                    <p><strong>🎯 Topic:</strong> [Event Topic/Theme]</p>
                </div>
            </div>
            
            <div style="text-align: center; margin: 30px 0;">
                <a href="#" style="background: #ff6b6b; color: white; padding: 15px 30px; 
                   text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block;">
                    Register Now
                </a>
            </div>
            """);
    }

    public String getTemplateContent(String templateName) {
        return TEMPLATES.getOrDefault(templateName, "<p>Start building your newsletter content here...</p>");
    }

    public Set<String> getAvailableTemplates() {
        return TEMPLATES.keySet();
    }

    public boolean hasTemplate(String templateName) {
        return TEMPLATES.containsKey(templateName);
    }
}