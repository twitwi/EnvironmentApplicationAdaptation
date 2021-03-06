/*
BUILD: gcc -L/usr/X11R6/lib -o ../build/sendxevent sendxevent.c -lX11 
*/
/*      $Id: irxevent.c,v 5.6 2001/11/20 15:12:33 ranty Exp $      */

/****************************************************************************
 ** irxevent.c **************************************************************
 ****************************************************************************
 *
 * irxevent  - infra-red xevent sender
 *
 * Heinrich Langos  <heinrich@null.net>
 * small modifications by Christoph Bartelmus <lirc@bartelmus.de>
 *
 * irxevent is based on irexec (Copyright (C) 1998 Trent Piepho)
 * and irx.c (no copyright notice found)
 *
 *  =======
 *  HISTORY
 *  =======
 *
 * 0.1 
 *     -Initial Release
 *
 * 0.2 
 *     -no more XWarpPointer... sending Buttonclicks to off-screen
 *      applications works becaus i also fake the EnterNotify and LeaveNotify
 *     -support for keysymbols rather than characters... so you can use
 *      Up or Insert or Control_L ... maybe you could play xquake than :*)
 *
 * 0.3
 *     -bugfix for looking for subwindows of non existing windows 
 *     -finaly a README file
 *
 * 0.3a (done by Christoph Bartelmus)
 *     -read from a shared .lircrc file 
 *     -changes to comments
 *     (chris, was that all you changed?)
 *
 * 0.4
 *     -fake_timestamp() to solve gqmpeg problems 
 *     -Shift Control and other mod-keys may work. (can't check it right now)
 *      try ctrl-c or shift-Page_up or whatever ...
 *      modifiers: shift, caps, ctrl, alt, meta, numlock, mod3, mod4, scrlock
 *     -size of 'char *keyname' changed from 64 to 128 to allow all mod-keys. 
 *     -updated irxevent.README
 *
 * 0.4.1
 *     -started to make smaller version steps :-)
 *     -Use "CurrentWindow" as window name to send events to the window
 *      that -you guessed it- currently has the focus.
 *
 * 0.4.2
 *     -fixed a stupid string bug in key sending.
 *     -updated irxevent.README to be up to date with the .lircrc format.
 *
 * 0.4.3
 *     -changed DEBUG functions to actually produce some output :)
 *
 * 0.5.0
 *     -fixed finding subwindows recursively
 *     -added xy_Key (though xterm and xemacs still don�t like me)
 *     -added compilation patch from Ben Hochstedler 
 *      <benh@eeyore.moneng.mei.com> for compiling on systems 
 * 	without strsep() (like some solaris)
 *
 *
 * see http://www.wh9.tu-dresden.de/~heinrich/lirc/irxevent/irxevent.keys
 * for a the key names. (this one is for you Pablo :-) )
 *
 * for more information see the irxevent.README file
 *
 */

#ifdef HAVE_CONFIG_H
# include <config.h>
#endif

#include <errno.h>
#include <unistd.h>
#include <getopt.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/stat.h>
#include <sys/types.h>

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <sys/time.h>
#include <unistd.h>

#define VERSION 0.1

#define DEBUG 1 
#undef DEBUG
#ifdef DEBUG
void debugprintf(char *format_str, ...)
{
        va_list ap;
        va_start(ap,format_str);
        vfprintf(stderr,format_str,ap);
        va_end(ap);
}
#else
void debugprintf(char *format_str, ...)
{
}
#endif


struct keymodlist_t {
        char *name;
        Mask mask;
};
static struct keymodlist_t keymodlist[]=
{
  {"SHIFT",   ShiftMask},
  {"CAPS",    LockMask},
  {"CTRL",    ControlMask},
  {"ALT",     Mod1Mask},{"META",    Mod1Mask},
  {"NUMLOCK", Mod2Mask},
  {"MOD3",    Mod3Mask},  /* I don't have a clue what key maps to this. */
  {"MOD4",    Mod4Mask},  /* I don't have a clue what key maps to this. */
  {"SCRLOCK", Mod5Mask},
  {NULL,0},
};

const char *key_delimiter ="-";
const char *active_window_name ="CurrentWindow";


char *progname;
Display *dpy;
Window root;
XEvent xev;
Window w,subw;

Time fake_timestamp()
     /*seems that xfree86 computes the timestamps like this     */
     /*strange but it relies on the *1000-32bit-wrap-around     */
     /*if anybody knows exactly how to do it, please contact me */
{
  int  tint;
  struct timeval  tv;
  struct timezone tz; /* is not used since ages */
  gettimeofday(&tv,&tz);
  tint=(int)tv.tv_sec*1000;
  tint=tint/1000*1000;
  tint=tint+tv.tv_usec/1000;
  return (Time)tint;
}

 Window find_window(Window top,char *name)
 {
   char *wname,*iname;
   XClassHint xch;
   Window *children,foo;
   int revert_to_return;
   unsigned int nc;
   if (!strcmp(active_window_name,name)){
     XGetInputFocus(dpy, &foo, &revert_to_return);
     return(foo);
   }
   {
     char buf[128];
     sprintf(buf, "0x%lx", top);
     if (!strcmp(name, buf)) {
       return top;
     }
   }
   /* First the base case */
   if (XFetchName(dpy,top,&wname)){
     if (!strncmp(wname,name,strlen(name)))  {
       XFree(wname);
       debugprintf("found it by wname %x \n",top);
       return(top);  /* found it! */
     };
     XFree(wname);
   };

   if(XGetIconName(dpy,top,&iname)){
     if (!strncmp(iname,name,strlen(name)))  {
       XFree(iname);
       debugprintf("found it by iname %x \n",top);
       return(top);  /* found it! */
     };
     XFree(iname);
   };

   if(XGetClassHint(dpy,top,&xch))  {
     if(!strcmp(xch.res_class,name))  {
       XFree(xch.res_name); XFree(xch.res_class);
       debugprintf("res_class '%s' res_name '%s' %x \n", xch.res_class,xch.res_name,top);
       return(top);  /* found it! */
     };
     if(!strcmp(xch.res_name,name))  {
       XFree(xch.res_name); XFree(xch.res_class);
       debugprintf("res_class '%s' res_name '%s' %x \n", xch.res_class,xch.res_name,top);
       return(top);  /* found it! */
     };
     XFree(xch.res_name); XFree(xch.res_class);
   };

   if(!XQueryTree(dpy,top,&foo,&foo,&children,&nc) || children==NULL) {
     return(0);  /* no more windows here */
   };

   /* check all the sub windows */
   for(;nc>0;nc--)  {
     top = find_window(children[nc-1],name);
     if(top) break;  /* we found it somewhere */
   };
   if(children!=NULL) XFree(children);
   return(top);
 }

 Window find_sub_sub_window(Window top,int *x, int *y)
 {
   Window base;
   Window *children,foo,target=0;
   unsigned int nc,
     rel_x,rel_y,width,height,border,depth,
     new_x=1,new_y=1,
     targetsize=1000000;

   base=top;
   if (!base) {return base;};
   if(!XQueryTree(dpy,base,&foo,&foo,&children,&nc) || children==NULL) {
     return(base);  /* no more windows here */
   };
   debugprintf("found subwindows %d\n",nc);

   /* check if we hit a sub window and find the smallest one */
   for(;nc>0;nc--)  {
     if(XGetGeometry(dpy, children[nc-1], &foo, &rel_x, &rel_y, 
                     &width, &height, &border, &depth)){
       if ((rel_x<=*x)&&(*x<=rel_x+width)&&(rel_y<=*y)&&(*y<=rel_y+height)){
         debugprintf("found a subwindow %x +%d +%d  %d x %d   \n",children[nc-1], rel_x,rel_y,width,height);
         if ((width*height)<targetsize){
           target=children[nc-1];
           targetsize=width*height;
           new_x=*x-rel_x;
           new_y=*y-rel_y;
           /*bull's eye ...*/
           target=find_sub_sub_window(target,&new_x,&new_y);
         }
       }	
    }
  };
  if(children!=NULL) XFree(children);
  if (target){
    *x=new_x;
    *y=new_y;
    return target;
  }else
    return base;
}



Window find_sub_window(Window top,char *name,int *x, int *y)
{
  Window base;
  Window *children,foo,target=0;
  unsigned int nc,
    rel_x,rel_y,width,height,border,depth,
    new_x=1,new_y=1,
    targetsize=1000000;

  base=find_window(top, name);
  if (!base) {return base;};
  if(!XQueryTree(dpy,base,&foo,&foo,&children,&nc) || children==NULL) {
    return(base);  /* no more windows here */
  };
  debugprintf("found subwindows %d\n",nc);

  /* check if we hit a sub window and find the smallest one */
  for(;nc>0;nc--)  {
    if(XGetGeometry(dpy, children[nc-1], &foo, &rel_x, &rel_y, 
		    &width, &height, &border, &depth)){
      if ((rel_x<=*x)&&(*x<=rel_x+width)&&(rel_y<=*y)&&(*y<=rel_y+height)){
	debugprintf("found a subwindow %x +%d +%d  %d x %d   \n",children[nc-1], rel_x,rel_y,width,height);
	if ((width*height)<targetsize){
	  target=children[nc-1];
	  targetsize=width*height;
	  new_x=*x-rel_x;
	  new_y=*y-rel_y;
	  /*bull's eye ...*/
	  target=find_sub_sub_window(target,&new_x,&new_y);
	}
      }	
    }
  };
  if(children!=NULL) XFree(children);
  if (target){
    *x=new_x;
    *y=new_y;
    return target;
  }else
    return base;
}


void make_button(int button,int x,int y,XButtonEvent *xev)
{
  xev->type = ButtonPress;
  xev->display=dpy;
  xev->root=root;
  xev->subwindow=None;
  xev->time=fake_timestamp();
  xev->x=x;xev->y=y;
  xev->x_root=1; xev->y_root=1;
  xev->state=0;
  xev->button=button;
  xev->same_screen=True;

  return;
}

void make_key(char *keyname,int x, int y,XKeyEvent *xev)
{
  char *part, *part2;
  struct keymodlist_t *kmlptr;
  part2=malloc(128);

  xev->type = KeyPress;
  xev->display=dpy;
  xev->root=root;
  xev->subwindow = None;
  xev->time=fake_timestamp();
  xev->x=x; xev->y=y;
  xev->x_root=1; xev->y_root=1;
  xev->same_screen = True;

  xev->state=0;
  while ((part=strsep(&keyname, key_delimiter)))
    {
      part2=strncpy(part2,part,128);
      //      debugprintf("-   %s \n",part);
      kmlptr=keymodlist;
      while (kmlptr->name)
	{
	  //	  debugprintf("--  %s %s \n", kmlptr->name, part);
	  if (!strcasecmp(kmlptr->name, part))
	    xev->state|=kmlptr->mask;
	  kmlptr++;
	}
        //    debugprintf("--- %s \n",part);i    
    } 
    //debugprintf("*** %s \n",part);
    //debugprintf("*** %s \n",part2);
  if (!strncmp("0x", part2, 2)) {
    sscanf(part2+2, "%X", &xev->keycode);
    xev->keycode = XKeysymToKeycode(dpy, xev->keycode);
  } else {
    xev->keycode=XKeysymToKeycode(dpy,XStringToKeysym(part2));
  }
  
  //debugprintf("state 0x%x, keycode 0x%x\n",xev->state, xev->keycode);
  free(part2);
  return ;
}

void sendfocus(Window w,int in_out)
{
  XFocusChangeEvent focev;

  focev.display=dpy;
  focev.type=in_out;
  focev.window=w;
  focev.mode=NotifyNormal;
  focev.detail=NotifyPointer;
  XSendEvent(dpy,w,True,FocusChangeMask,(XEvent*)&focev);
  XSync(dpy,True);
  
  return;
}

void sendpointer_enter_or_leave(Window w,int in_out)
{
  XCrossingEvent crossev;
  crossev.type=in_out;
  crossev.display=dpy;
  crossev.window=w;
  crossev.root=root;
  crossev.subwindow=None;
  crossev.time=fake_timestamp();
  crossev.x=1;
  crossev.y=1;
  crossev.x_root=1;
  crossev.y_root=1;
  crossev.mode=NotifyNormal;
  crossev.detail=NotifyNonlinear;
  crossev.same_screen=True;
  crossev.focus=True;
  crossev.state=0;
  XSendEvent(dpy,w,True,EnterWindowMask|LeaveWindowMask,(XEvent*)&crossev);
  XSync(dpy,True);
  return;
}

void sendkey(char *keyname,int x,int y,Window w,Window s)
{
  debugprintf("sendkey: %s\n", keyname);
  make_key(keyname ,x,y,(XKeyEvent*)&xev);
  debugprintf("sendkey: made key\n");
  xev.xkey.window=w;
  xev.xkey.subwindow=s;

  if (s) sendfocus(s,FocusIn);

  XSendEvent(dpy,w,True,KeyPressMask,&xev);
  xev.type = KeyRelease;
  usleep(2000);
  xev.xkey.time = fake_timestamp();
  if (s) sendfocus(s,FocusOut);
  XSendEvent(dpy,w,True,KeyReleaseMask,&xev);
  XSync(dpy,True);
  debugprintf("Sent key.\n");
  return;
}

void sendkeypress(char *keyname,int x,int y,Window w,Window s)
{
  debugprintf("sendkeypress: %s\n", keyname);
  make_key(keyname ,x,y,(XKeyEvent*)&xev);
  debugprintf("sendkeypress: made key\n");
  xev.xkey.window=w;
  xev.xkey.subwindow=s;

  if (s) sendfocus(s,FocusIn);

  XSendEvent(dpy,w,True,KeyPressMask,&xev);
  //xev.type = KeyRelease;
  //usleep(2000);
  xev.xkey.time = fake_timestamp();
  if (s) sendfocus(s,FocusOut);
  //XSendEvent(dpy,w,True,KeyReleaseMask,&xev);
  XSync(dpy,True);
  debugprintf("Sent keypress.\n");
  return;
}
void sendkeyrelease(char *keyname,int x,int y,Window w,Window s)
{
  debugprintf("sendkeyrelease: %s\n", keyname);
  make_key(keyname ,x,y,(XKeyEvent*)&xev);
  debugprintf("sendkeyrelease: made key\n");
  xev.xkey.window=w;
  xev.xkey.subwindow=s;

  if (s) sendfocus(s,FocusIn);

  xev.type = KeyRelease;
  XSendEvent(dpy,w,True,KeyPressMask,&xev);
  //xev.type = KeyRelease;
  //usleep(2000);
  xev.xkey.time = fake_timestamp();
  if (s) sendfocus(s,FocusOut);
  //XSendEvent(dpy,w,True,KeyReleaseMask,&xev);
  XSync(dpy,True);
  debugprintf("Sent keyrelease.\n");
  return;
}
void sendbutton(int button, int x, int y, Window w,Window s)
{
  make_button(button,x,y,(XButtonEvent*)&xev);
  xev.xbutton.window=w;
  xev.xbutton.subwindow=s;
  sendpointer_enter_or_leave(w,EnterNotify);
  sendpointer_enter_or_leave(s,EnterNotify);

  XSendEvent(dpy,w,True,ButtonPressMask,&xev);
  XSync(dpy,True);
  xev.type = ButtonRelease;
  xev.xkey.state|=0x100;
  usleep(1000);
  xev.xkey.time = fake_timestamp(); 
  XSendEvent(dpy,w,True,ButtonReleaseMask,&xev);
  sendpointer_enter_or_leave(s,LeaveNotify);
  sendpointer_enter_or_leave(w,LeaveNotify);
  XSync(dpy,True);

  return;
}


int check(char *s)
{
  int d;
  char *buffer;

  buffer=malloc(strlen(s));
  if(buffer==NULL)
    {
      fprintf(stderr,"%s: out of memory\n",progname);
      return(-1);
    }

  if(1!=sscanf(s,"Focus %s\n",buffer,buffer) &&
     2!=sscanf(s,"Key %s %s\n",buffer,buffer) &&
     2!=sscanf(s,"KeyPress %s %s\n",buffer,buffer) &&
     2!=sscanf(s,"KeyRelease %s %s\n",buffer,buffer) &&
     4!=sscanf(s,"Button %d %d %d %s\n",&d,&d,&d,buffer) &&
     4!=sscanf(s,"xy_Key %d %d %s %s\n",&d,&d,buffer,buffer))
    {
      fprintf(stderr,"%s: bad config string \"%s\"\n",progname,s);
      free(buffer);
      return(-1);
    }
  free(buffer);  
  return(0);
}

static struct option long_options[] =
{
	{"help", no_argument, NULL, 'h'},
	{"version", no_argument, NULL, 'V'},
	{0, 0, 0, 0}
};

static void processLine(char* code) {
  char keyname[128];
  int pointer_button,pointer_x,pointer_y;
  char windowname[128];
	      debugprintf("Recieved code: %s\nSending event: ",code);
              if (1==0) {}
	      else if(1==sscanf(code,"Focus %s\n",windowname)) {
                if((w=find_window(root,active_window_name)))
		    {
		      debugprintf("windowname: %s\n",active_window_name);
                      sendfocus(w, FocusOut);
		    }
		  else
		    {
		      debugprintf("target window '%s' not found \n",active_window_name);
		    }

                if((w=find_window(root,windowname)))
		    {
		      debugprintf("windowname: %s\n",windowname);
                      sendfocus(w, FocusIn);
		    }
		  else
		    {
		      debugprintf("target window '%s' not found \n",windowname);
		    }

              }
	      else if(2==sscanf(code,"KeyPress %s %s\n",keyname,windowname))
		{
		  if((w=find_window(root,windowname)))
		    {
		      debugprintf("keyname: %s \t windowname: %s\n",keyname,windowname);
		      sendkeypress(keyname,1,1,w,0);
		    }
		  else
		    {
		      debugprintf("target window '%s' not found \n",windowname);
		    }
		}
	      else if(2==sscanf(code,"KeyRelease %s %s\n",keyname,windowname))
		{
		  if((w=find_window(root,windowname)))
		    {
		      debugprintf("keyname: %s \t windowname: %s\n",keyname,windowname);
		      sendkeyrelease(keyname,1,1,w,0);
		    }
		  else
		    {
		      debugprintf("target window '%s' not found \n",windowname);
		    }
		}
	      else if(2==sscanf(code,"Key %s %s\n",keyname,windowname))
		{
		  if((w=find_window(root,windowname)))
		    {
		      debugprintf("keyname: %s \t windowname: %s\n",keyname,windowname);
		      sendkey(keyname,1,1,w,0);
		    }
		  else
		    {
		      debugprintf("target window '%s' not found \n",windowname);
		    }
		}
	      else if(4==sscanf(code,"Button %d %d %d %s\n",
				&pointer_button,&pointer_x,
				&pointer_y,windowname))
		{
		  
		  if((w=find_window(root,windowname)) &&
		     (subw=find_sub_window(root,windowname,&pointer_x,&pointer_y)))
		    {
		      if (w==subw) subw=0;
		      //debugprintf(" %s\n",c);
		      sendbutton(pointer_button,pointer_x,pointer_y,w,subw);
		    }
		  else
		    {
		      debugprintf("target window '%s' not found \n",windowname);
		    }
		}
	      else if(4==sscanf(code,"xy_Key %d %d %s %s\n",
				&pointer_x,&pointer_y,
				keyname,windowname))
		{
		  
		  if((w=find_window(root,windowname))&& (subw=find_sub_window(root,windowname,&pointer_x,&pointer_y)))
		    {
		      debugprintf(" %s\n",code);
		      if (w==subw) subw=0;
		      sendkey(keyname,pointer_x,pointer_y,w,subw);
		    }
		  else
		    {
		      debugprintf("target window '%s' not found \n",windowname);
		    }
		}
}  

int main(int argc, char *argv[])
{
  int c;
  char *code;

  progname=argv[0];

  while ((c = getopt_long(argc, argv, "hV", long_options, NULL)) != EOF) {
    switch (c) {
    case 'h':
      printf("Usage: %s [options]\n", argv[0]);
      printf("\t -k --keyname \t\tkeyname\n");
      printf("\t -w --window \t\twindow name\n");
      printf("\t -h --help \t\tdisplay usage summary\n");
      printf("\t -V --version \t\tdisplay version\n");
      return(EXIT_SUCCESS);
    case 'V':
      printf("%s %s\n", progname, VERSION);
      return(EXIT_SUCCESS);
    case '?':
      fprintf(stderr, "unrecognized option: -%c\n", optopt);
      fprintf(stderr, "Try `%s --help' for more information.\n", progname);
      return(EXIT_FAILURE);
    }
  }
  if (argc == optind+1){
    code = argv[optind];
  } else if (argc > optind+1){
    fprintf(stderr, "%s: incorrect number of arguments.\n", progname);
    fprintf(stderr, "Try `%s --help' for more information.\n", progname);
    return(EXIT_FAILURE);
  }

  dpy=XOpenDisplay(NULL);
  if(dpy==NULL) {
    fprintf(stderr,"Can't open DISPLAY.\n");
    exit(1);
  }
  root=RootWindow(dpy,DefaultScreen(dpy));
  debugprintf( "code = %s\n", code);

//  keyname = malloc(sizeof(char)*256);
//  windowname = malloc(sizeof(char)*256);
  if (argc == 1) {
    printf("Reading from stdin\n");
    char line[1023];
    while (!feof(stdin)) {
      fgets(line, 1023, stdin);
      processLine(line);
    }
    exit(0);
  } else {
    processLine(code);
  }
  
  exit(0);
}
