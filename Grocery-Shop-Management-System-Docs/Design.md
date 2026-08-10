# Grocery Shop Management System --- Design System

## 1. Design Goal

The application should feel like a practical business tool rather than a
flashy portfolio website.

The primary design priorities are:

1.  Clarity.
2.  Speed.
3.  Readability.
4.  Mobile usability.
5.  Consistency.
6.  Accessibility.

The shop owner should be able to understand the interface without
technical knowledge.

## 2. Visual Direction

Use a clean, modern, lightweight dashboard style.

Avoid:

-   Excessive gradients.
-   Large decorative illustrations.
-   Heavy animations.
-   Glassmorphism everywhere.
-   Excessive shadows.
-   Crowded dashboards.
-   Tiny text.

The UI should feel professional and trustworthy.

## 3. Color System

Use a neutral base with a green primary color because it fits the
grocery/retail context.

### Primary

``` text
Primary: #16A34A
Primary Hover: #15803D
Primary Light: #DCFCE7
```

### Neutral

``` text
Background: #F8FAFC
Surface: #FFFFFF
Border: #E2E8F0
Text Primary: #0F172A
Text Secondary: #64748B
Muted: #94A3B8
```

### Semantic

``` text
Success: #16A34A
Warning: #F59E0B
Danger: #DC2626
Info: #2563EB
```

Do not use colors only to communicate meaning. Pair status colors with
text/icons where appropriate.

## 4. Typography

Use a modern sans-serif font.

Preferred:

``` text
Inter
```

Fallback:

``` text
system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif
```

### Suggested scale

``` text
Page title: 24–28px
Section title: 18–20px
Card title: 16px
Body: 14–16px
Small/meta: 12–13px
```

Prioritize readability over fitting more information on screen.

## 5. Spacing

Use a consistent spacing system based primarily on multiples of 4px.

Examples:

``` text
4px
8px
12px
16px
20px
24px
32px
40px
48px
```

Avoid arbitrary spacing values unless necessary.

## 6. Layout

### Desktop

Use:

``` text
Sidebar
   +
Main Content
```

Example:

``` text
┌──────────────┬─────────────────────────────┐
│              │ Header                      │
│   Sidebar    ├─────────────────────────────┤
│              │                             │
│ Dashboard    │ Main Content                │
│ Products     │                             │
│ Inventory    │                             │
│ Sales        │                             │
│ Reports      │                             │
│              │                             │
└──────────────┴─────────────────────────────┘
```

### Mobile

The sidebar should become a compact navigation mechanism.

Prioritize:

-   Dashboard.
-   Products.
-   Inventory.
-   New Sale.

## 7. Navigation

Primary navigation:

``` text
Dashboard
Products
Inventory
Sales
Reports
Settings
```

Keep navigation labels explicit.

Avoid icon-only navigation unless the meaning is obvious and accessible
labels are provided.

## 8. Dashboard Design

Use a small number of summary cards.

Example:

``` text
┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐
│ Sales Today│ │ Profit     │ │ Low Stock  │ │ Products   │
│ ₹4,250     │ │ ₹620       │ │ 8          │ │ 324        │
└────────────┘ └────────────┘ └────────────┘ └────────────┘
```

Below the cards:

-   Recent sales.
-   Low-stock list.
-   Useful quick actions.

Do not make the dashboard dependent on complex charts.

## 9. Product List

Recommended columns:

``` text
Product
Category
Selling Price
Stock
Unit
Status
Actions
```

On mobile, transform the table into cards or a horizontally scrollable
table where appropriate.

## 10. Stock Status

Use clear labels:

``` text
In Stock
Low Stock
Out of Stock
```

Suggested visual treatment:

-   In Stock → success.
-   Low Stock → warning.
-   Out of Stock → danger.

Always include text in addition to color.

## 11. Forms

Forms should:

-   Have clear labels.
-   Use sensible defaults.
-   Show validation close to the relevant field.
-   Clearly indicate required fields.
-   Prevent invalid submissions.
-   Provide clear success/error feedback.

Example product form:

``` text
Product Name *
Category *
Brand
Unit *
Purchase Price *
Selling Price *
Current Quantity
Minimum Stock Level

[Cancel] [Save Product]
```

## 12. Buttons

Primary action:

``` text
Save Product
Complete Sale
Add Stock
```

Secondary action:

``` text
Cancel
Back
Filter
```

Danger action:

``` text
Delete
Deactivate
```

Destructive actions should require confirmation when data loss is
possible.

## 13. Sales Screen

The sales workflow should be one of the fastest screens.

Recommended layout:

``` text
Search product
        ↓
Select product
        ↓
Add quantity
        ↓
Cart
        ↓
Total
        ↓
Complete Sale
```

The user should not need to navigate through multiple pages to add a
simple sale.

## 14. Feedback

Use:

-   Toasts for short success messages.
-   Inline validation for form errors.
-   Clear empty states.
-   Loading indicators for API operations.
-   Confirmation dialogs for destructive actions.

Avoid intrusive notifications.

## 15. Responsive Design

The application must work on:

-   Desktop.
-   Laptop.
-   Android phone.
-   Tablet.

Primary mobile use cases:

-   Checking stock.
-   Searching products.
-   Recording sales.
-   Adding stock.

## 16. Accessibility

Follow basic accessibility principles:

-   Semantic HTML.
-   Keyboard-accessible controls.
-   Visible focus states.
-   Labels for inputs.
-   Sufficient text contrast.
-   Accessible buttons.
-   Do not rely only on color.

## 17. Animation

Use minimal animation.

Acceptable:

-   Small hover transitions.
-   Button state transitions.
-   Modal appearance.
-   Loading indicators.

Avoid animation that slows down shop workflows.

## 18. Branding

The application can initially use a simple name such as:

**Grocery Manager**

The final product name can be changed later.

Use a simple text/logo treatment rather than spending development time
on elaborate branding.

## 19. Design Principle

Every design decision should answer:

> "Does this help the shop owner manage the shop faster and with fewer
> mistakes?"

If not, it probably does not belong in Version 1.
