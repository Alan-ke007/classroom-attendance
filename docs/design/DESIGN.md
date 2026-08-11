# 智课考勤 Design System

## Colors
- **Primary**: `#007AFF` (light) / `#0A84FF` (dark)
- **Primary Dark**: `#0055CC` / `#007AFF`
- **Background**: `#F2F2F7` (light) / `#0D0D0F` (dark)
- **Card**: `#FFFFFF` / `#1C1C1E`
- **Text Primary**: `#1D1D1F` / `#F5F5F7`
- **Text Secondary**: `#86868B` / `#98989D`
- **Success**: `#34C759` / `#30D158`
- **Warning**: `#FF9500` / `#FF9F0A`
- **Danger**: `#FF3B30` / `#FF453A`
- **Glass BG**: `rgba(255,255,255,0.18)` / `rgba(255,255,255,0.06)`
- **Glass Border**: `rgba(255,255,255,0.30)` / `rgba(255,255,255,0.10)`

## Typography
- **Font stack**: `-apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif`
- **Base size**: 15px
- **Heading scale**: 28px/22px/18px/16px (h1-h4)
- **Line height**: 1.6 body, 1.3-1.45 headings

## Spacing
- Radius scale: 8px/12px/16px/20px (sm/md/lg/xl)
- Card padding: 20px-24px
- Section gap: 20px

## Elevation
- Shadow sm: `0 1px 3px rgba(0,0,0,0.04)` (light) / `0 1px 3px rgba(0,0,0,0.20)` (dark)
- Shadow md: `0 4px 12px rgba(0,0,0,0.06)` / `0 4px 12px rgba(0,0,0,0.25)`
- Shadow lg: `0 8px 30px rgba(0,0,0,0.10)` / `0 8px 30px rgba(0,0,0,0.35)`
- Glass shadow: `0 8px 32px rgba(0,0,0,0.08)` / `0 8px 32px rgba(0,0,0,0.30)`

## Components
- **Cards**: glass effect with backdrop-filter, 1px glass border, rounded corners
- **Buttons**: gradient primary, subtle hover lift + glow shadow
- **Sidebar**: glass background, backdrop-filter blur, border-right separator
- **Tables**: striped rows, hover highlight, responsive pagination
- **Charts**: ECharts with dark/light theme switching

## Motion
- Transition: `0.3s cubic-bezier(0.25, 0.1, 0.25, 1)`
- Page transitions: fade-slide (12px vertical)
- Hover: subtle translateY(-1px to -4px) + enhanced shadow
- Menu items: color/bg transition on hover and active states
