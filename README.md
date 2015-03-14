# Matrix Views

## Description
Several spreadsheet-style views of an ontology, including existential fillers, individual relationships and an object properties view.

## Features
- Navigate by fully functioning class/property tree
- Add/remove columns to customize - show any combination of annotations/properties/features
- Comma-separate values in a list for quick editing
- Drag and drop object or data properties on the matrices to add columns
- Drag and drop classes on the matrices to add fillers, domains and ranges
- Filter annotation columns by language (to allow translation-style view)
- Filter property columns by restriction type (some or only supported)
- Full class expressions supported in editors

# The plugins
Window | Tabs menu
Two default tab layouts are provided:

### Matrix
Property Matrix
Window | Views menu
All of the matrices are implemented as views so you can add them into your own custom tabs:

### Class Views
- Class annotations
- Class matrix (asserted and inferred hierarchies) - previously Class Existential MAtrix
- For highly compositional ontologies a lot of time can be saved by using the Class Matrix to add multiple superclass restrictions on classes.

- Add property columns to the matrix, either by drag and drop or using the toolbar add object/data property column to matrix. - Dnd will default to some restrictions, but with the add button you can specify this.
- Some cells may already contain values. There can be several fillers in one cell. See below for the meaning of the highlighting.
- Add values by dragging classes onto the cell or edit by clicking in a cell to start a text editor.
- The column specifies the property and the restriction type.

if a cell value is

- plain - filler of a restriction in a subclass (can be edited)
- bold - filler of a restriction in an equivalent class (not affected by editing)
- (bracketed) - this means it is an inherited from an ancestor (not affected by editing)

Multiple values in cells are separated by commas and each value is a filler for a separate sub/equivalent class restriction.

Eg

p (some)	q (only)
SuperA	ClassE	
--ClassA	ClassB ClassC, ClassD (ClassE	ClassF
Then the ontology contains:

equivalentClass(ClassA, p some ClassB) actually p some ClassB may be in an intersection

subClassOf(ClassA, p some ClassC)

subClassOf(ClassA, p some ClassD)

subClassOf(SuperA, p some ClassE) shows up as inherited for all subs of SuperA

subClassOf(ClassA, q only ClassF)

### Individual Views

- Class membership
- Property Assertions - previously Individual Relationships

### Object Property Views
- Object property matrix
- Data Property Views
- Data property matrix

## Quick Start

### Translation
To aid multilingual label generation for entities, all matrix views allow filtering by language. To set up a view as below, follow these steps:

- Enable one of the matrix tabs or add a matrix view to a current tab
- Press the top left button on the menu bar Add annotation column to matrix. A dialog will appear.
- Select the annotation URI in which you will create your labels
- Select a language you will be adding labels for
- Press OK
- If you want to provide multiple translations or compare against an existing translation repeat from step 2, but select a different language
- Open up your hierarchy and select the first cell you wish to edit
- When entered, return drops to the next entity and you can start typing immediately

## Author
Nick Drummond, The University of Manchester

## License
LGPL
