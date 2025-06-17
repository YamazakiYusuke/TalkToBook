#!/usr/bin/env python3
"""
WCAG AA Contrast Compliance Verification for TalkToBook
Verifies that all color combinations meet accessibility standards
"""

def hex_to_rgb(hex_color):
    """Convert hex color to RGB tuple"""
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))

def luminance(rgb):
    """Calculate relative luminance of a color"""
    def transform(c):
        c = c / 255.0
        return c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4
    
    r, g, b = [transform(c) for c in rgb]
    return 0.2126 * r + 0.7152 * g + 0.0722 * b

def contrast_ratio(color1, color2):
    """Calculate contrast ratio between two colors"""
    lum1 = luminance(hex_to_rgb(color1))
    lum2 = luminance(hex_to_rgb(color2))
    
    lighter = max(lum1, lum2)
    darker = min(lum1, lum2)
    
    return (lighter + 0.05) / (darker + 0.05)

def check_compliance(foreground, background, min_ratio=4.5, description=""):
    """Check if color combination meets WCAG standards"""
    ratio = contrast_ratio(foreground, background)
    compliant = ratio >= min_ratio
    status = "✓ PASS" if compliant else "✗ FAIL"
    print(f"{description}: {ratio:.2f} {status} (min: {min_ratio})")
    return compliant

def main():
    print("=== TalkToBook WCAG AA Contrast Verification ===")
    print()
    
    # Color definitions from TalkToBook theme
    colors = {
        'Primary': '#1565C0',
        'Secondary': '#2E7D32', 
        'Background': '#FFFFFF',
        'Surface': '#FAFAFA',
        'OnPrimary': '#FFFFFF',
        'OnSecondary': '#FFFFFF',
        'OnBackground': '#212121',
        'OnSurface': '#212121',
        'Error': '#D32F2F',
        'OnError': '#FFFFFF',
        'FocusIndicator': '#FF6F00',
        'ButtonPressed': '#0277BD',
        'Divider': '#BDBDBD',
        'Disabled': '#9E9E9E'
    }
    
    print("WCAG AA Compliance Results:")
    print("==========================")
    
    all_compliant = True
    
    # Primary combinations
    all_compliant &= check_compliance(colors['Primary'], colors['Background'], 4.5, "Primary on Background")
    all_compliant &= check_compliance(colors['OnPrimary'], colors['Primary'], 4.5, "OnPrimary on Primary")
    
    # Secondary combinations  
    all_compliant &= check_compliance(colors['Secondary'], colors['Background'], 4.5, "Secondary on Background")
    all_compliant &= check_compliance(colors['OnSecondary'], colors['Secondary'], 4.5, "OnSecondary on Secondary")
    
    # Background combinations
    all_compliant &= check_compliance(colors['OnBackground'], colors['Background'], 4.5, "OnBackground on Background")
    all_compliant &= check_compliance(colors['OnSurface'], colors['Surface'], 4.5, "OnSurface on Surface")
    
    # Error combinations
    all_compliant &= check_compliance(colors['Error'], colors['Background'], 4.5, "Error on Background")
    all_compliant &= check_compliance(colors['OnError'], colors['Error'], 4.5, "OnError on Error")
    
    print()
    print("Additional Accessibility Checks:")
    print("===============================")
    
    # Focus indicator (non-text, 3:1 minimum)
    all_compliant &= check_compliance(colors['FocusIndicator'], colors['Background'], 3.0, "Focus Indicator vs Background")
    
    # Button pressed state
    all_compliant &= check_compliance(colors['OnPrimary'], colors['ButtonPressed'], 4.5, "Text vs Pressed Button")
    
    # Divider (non-text, 3:1 minimum)
    all_compliant &= check_compliance(colors['Divider'], colors['Background'], 3.0, "Divider vs Background")
    
    # Disabled state
    all_compliant &= check_compliance(colors['Disabled'], colors['Background'], 4.5, "Disabled Text vs Background")
    
    print()
    
    # Overall assessment
    if all_compliant:
        print("🎉 ALL ACCESSIBILITY CHECKS PASSED!")
        print("The TalkToBook app meets WCAG AA standards for contrast ratios.")
    else:
        print("⚠️  Some accessibility checks failed.")
        print("Please review and adjust colors that don't meet WCAG AA standards.")
    
    print()
    print("WCAG Standards Reference:")
    print("- Normal text: 4.5:1 minimum contrast ratio")
    print("- Large text (18pt+): 3:1 minimum contrast ratio") 
    print("- Non-text elements: 3:1 minimum contrast ratio")
    print("- Focus indicators: 3:1 minimum contrast ratio")
    
    return all_compliant

if __name__ == "__main__":
    main()