  ligne 1 
  // [ifdef jse]
  ligne 2 
  ligne 3 
  // [enddef]
  // [ifndef jse]
  ligne 4 
  ligne 5 
  // [enddef]
  ligne 6 
  // [ifdef jse] uncomment
  //ligne 7 
  //ligne 8 
  // [enddef]
  //ligne 9 
  ligne 10 
  // [ifndef jse] uncomment
  //ligne 11 
  //ligne 12 
  // [enddef]
  //ligne 13 
  ligne 14 

// [ifndef jse]
ligne 15
// [enddef]

// [ifdef jse] uncomment
// ligne 16
// java.util.HashSet<ChallengeRequest>();
// [enddef]

// [ifdef jse] member uncomment
// ligne 17;
// ligne 18;

// [ifndef jse] member uncomment
// ligne 19;
// ligne 20;

// [ifdef jse] member
   ligne 21;
// ligne 22;

// [ifndef jse] member
// ligne 23;
// ligne 24;

// [ifdef jse] method uncomment
// public ligne25() {
// ligne 26
// ligne 27
// }
 

// [ifndef jse] method uncomment
// public ligne28() {
// ligne 29
// ligne 30
// }
 
// [ifdef jse] method
    public ligne31() {
       ligne 32
       ligne 33
    }
 
// [ifndef jse] method
    public ligne34() {
       ligne 35
       ligne 36
    }
 
// [ifdef jse] method
    public ligne37() {
       ligne 38
       ligne 39
    }
 
// [ifdef jse] line
 ligne 40
// [ifndef jse] line
// ligne 41
// [ifdef jse] line uncomment
// ligne 42
// ligne 43
// [ifndef jse] line uncomment
// ligne 44
// ligne 45

// [ifndef jse] javadocs
/** 
 * NJSE - Returns the next line to read, or null otherwise.
 * 
 * @return The next line to read, or null otherwise.
 * @throws IOException
 */
// [ifdef jse] javadocs
/** 
 * JSE - Returns the next line to read, or null otherwise.
 * 
 * @return The next line to read, or null otherwise.
 * @throws IOException
 */
ligne 46
ligne 47
// [ifdef gwt,android] javadocs
/** 
* GWT,ANDROID - Returns the next line to read, or null otherwise.
* 
* @return The next line to read, or null otherwise.
* @throws IOException
*/
