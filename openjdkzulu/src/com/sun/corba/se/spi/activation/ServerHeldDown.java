package com.sun.corba.se.spi.activation;


/**
* com/sun/corba/se/spi/activation/ServerHeldDown.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from c:/jenkins/workspace/zulu8-silver-build-win64/zulu-src/corba/src/share/classes/com/sun/corba/se/spi/activation/activation.idl
* Tuesday, January 12, 2021 7:48:15 AM PST
*/

public final class ServerHeldDown extends org.omg.CORBA.UserException
{
  public int serverId = (int)0;

  public ServerHeldDown ()
  {
    super(ServerHeldDownHelper.id());
  } // ctor

  public ServerHeldDown (int _serverId)
  {
    super(ServerHeldDownHelper.id());
    serverId = _serverId;
  } // ctor


  public ServerHeldDown (String $reason, int _serverId)
  {
    super(ServerHeldDownHelper.id() + "  " + $reason);
    serverId = _serverId;
  } // ctor

} // class ServerHeldDown
