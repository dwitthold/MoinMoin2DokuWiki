# MoinMoin2DokuWiki converter

### Overview
A basic converter for MoinMoin pages to DokuWiki pages. Pass the `--help` flag for info on run
 options.

Due to feature incompatibilities and the often unexpected behavior of MoinMoin's parser, the
 project is surprisingly complex. As a result the page histories are not preserved. There is also
 a significant number of incompatibilities, most of which are hopefully edge cases. Warnings
 about possible incompatibilities are logged to a file and can be configured (see below).

All resulting pages will have a FIXME warning at the page start if the appropriate flag was set. 

The converter will attempt to read MoinMoin's pages from `MoinMoin/wiki/data/pages`. Resulting
 pages will be put in `dokuwiki/data/pages`. Both directories can be changed.

A flag can be passed to run a 'tag cleaner' at the end, that removes empty tags (e.g. `****` for
 bold on and off) to improve readability.

The output charset is always UTF-8. The input charset can be configured.

`java.nio.file.NoSuchFileExceptions` are thrown if a page was deleted (but it might also be a genuine
 error).

MoinMoin seems to have at least one page besides the front page that is intended for internal use
 and should probably be deleted after conversion (BadContent).

Tested with MoinMoin 1.9.3 and DokuWiki 2018-04-22a "Greebo" on Linux.

This was primarily a personal project. MoinMoin2DokuWiki comes with ABSOLUTELY NO WARRANTY, to
 the extent permitted by applicable law.

### Warnings about incompatibilities
As some of MoinMoin's features are not available in DokuWiki and as MoinMoin's parser has some
 weird behavior (see also https://moinmo.in/MoinMoin2.0#Tree_based_transformations), there is a
 significant number of incompatibilities - most of which are hopefully edge cases. Examples are:
  * question marks in page names
  * using style-tags in table cells
  * successive asterisks are displayed in MoinMoin, but toggle bold formatting on or off in DokuWiki
  * some of MoinMoin's formatting can be applied over several lines or mixed with certain
   other formatting, other can't

During the conversion warnings about possible incompatibilities are logged to a file. The file is
 created in the current working directory and called `moinmoin2dokuwiki-warnings-{datetime}`. A
 list of known incompatibilities is stored in `resources/incompatibilities.json` (the list is very
 probably not complete). The list will be shown if the converter is started with the appropriate
 flag (see `--help`). The list can be overwritten by passing another file with incompatibilities
 via the command line (also see `--help`).

### Page Names
Certain characters (`?`, `:`) are not valid in DokuWiki page names and are replaced with
 underscores (`_`).
 
### Tables
Many functions from MoinMoin seem to be not possible in DokuWiki, only colspan and horizontal
 alignment are converted. Conversion of rowspan-tags is not implemented (yet?).

Formatting of cells via style-tag is not supported (except horizontal alignment).

### Indentation
For simple indentation (not lists) the number of spaces is kept even though indentation in
 DokuWiki is not possible. It might result in a 'code block' (when the indentation level is
  greater than two).
  
### Tag Cleaner
Is inactive by default and can be activated by passing the appropriate flag. Removes tags that are
 opened and closed right afterwards . Might make resulting "source pages" more readable, as e.g
 . blank lines will have those tags for active formats.

But it might also remove things like `****`. Those aren't displayed in DokuWiki anyway, but it
 might be easier to correct the page manually if they are still present.

### Also ignored/ impossible
Features that are (apparently) not available in regular DokuWiki:
 * smaller/ larger font - not available in DokuWiki
 * `[[attachment:filename.txt]]` - not available in DokuWiki (?)
 * Drawings - see https://moinmo.in/HelpOnDrawings
 * Definition lists
 * Macros and Variables
 * Parsers with CSS-highlighting
 * Admonitions
 * Comments

Features that were not implemented because the required development work seems disproportionate
 to the benefits:
 * Table of Contents - works differently in DokuWiki
 * Aligning and resizing of included (external) images
 * Horizontal Rules - DokuWiki does not support different thicknesses

Both lists are not guaranteed to be complete!

### Downloading and building the tool
 1. You need to have Git, Maven and JDK 11+ installed (e.g. `apt install git mvn openjdk-11-jdk`).
 1. Make sure JAVA_HOME is set to the correct JDK (`mvn -version`), e.g. by adding the variable to
  your .bashrc (depending on the shell you want to use to run Maven and your JDK location
   etc.): `JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/bin/`
 1. Clone the Git repository to download the source code: git clone https://github.com/dwitthold/MoinMoin2DokuWiki.git
 1. Change into the created repository, e.g. `cd MoinMoin2DokuWiki`
 1. Build the jar: `mvn clean package`

Execute the jar with dependencies you just build, e.g. 
`java -jar target/moinmoin2dokuwiki-0.1-SNAPSHOT-jar-with-dependencies.jar -h`
  
### Integrating the new pages into DokuWiki
 1. Backup your current DokuWiki installation (at least the data directory).
 1. Copy the generated pages to a new directory on the DokuWiki system (not the pages directory
  as you have to modify the files and there might be special pages in the pages directory whose
   existing ownership should not be changed).
 1. Change the ownership and group of the files to whatever Dokuwiki needs; on Linux:
    1. Look up the correct user and group by running `ls -ahl` in the pages directory of the
     DokuWiki installation (e.g. `dokuwiki/data/pages`); the third column shows the current owner
      of the existing pages, the fourth the group (e.g. dokuwiki_user and dokuwiki_group).
    1. `chown -R dokuwiki_user:dokuwiki_group *`
 1. Set the access permissions; on Linux: `chmod -R u+rwX,go+rX,go-w *`
 1. Move all files to the pages directory; on Linux: `cp -pR * /path/to/dokuwiki/data/pages` (`-p` to
  preserve permissions and ownership; `-R` for recursive copying of folders)
 1. Update DokuWiki's search index. The 
 ['Searchindex Manager'](https://www.dokuwiki.org/plugin:searchindex) worked for me.

### Mapping file for transliteration
While DokuWiki allows non-ASCII characters in internal links, the actual page files do not allow
 them. The Transliterator class converts MoinMoin (supper) page names into file names that are
 compatible to DokuWiki. Per default only certain common (like '!' -> '_'), german ('ä' -> 'a
 ') and french characters ('á' -> 'a') are supported. In order to add new or overwrite existing
 character mappings, a file path can be passed to MoinMoin2DokuWiki (see `--help`). The mappings
 are defined by pairs of lines: a line declaring the character (or sequence of characters; e.g
 . Ä) to be replaced is followed by a line with its replacement (e.g. A).

### Future Development
The differences between supported features in MoinMoin and DokuWiki and the inconsistent behavior
 of parsing rules in MoinMoin made the development surprisingly complex (and at times frustrating
). As this was a personal project to migrate my wiki, I will not continue to develop it.
 
Feel free to contact me, if you want to continue the work and need some help e.g. understanding
 the code. In order to document the gained knowledge about the parsing, a lot of unit
 tests were written, including for some features or edge cases that are not supported (yet?).

Preserving the editing history would probably be the most significant improvement. Reading
 a page's history in MoinMoin seems rather straightforward, but DokuWiki's history might work
 completely different.