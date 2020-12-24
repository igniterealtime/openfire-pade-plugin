<%
String auth = request.getHeader("Authorization");
String s = "";

//no auth, request NTLM
if (auth == null)
{
        response.setStatus(response.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "NTLM");
        return;
}

//check what client sent
if (auth.startsWith("NTLM "))
{
        byte[] msg =
           new sun.misc.BASE64Decoder().decodeBuffer(auth.substring(5));
        int off = 0, length, offset;

        if (msg[8] == 1) {
            off = 18;

            byte z = 0;
            byte[] msg1 =
                {(byte)'N', (byte)'T', (byte)'L', (byte)'M', (byte)'S',(byte)'S', (byte)'P',
                z,(byte)2, z, z, z, z, z, z, z,
                (byte)40, z, z, z, (byte)1, (byte)130, z, z,
                z, (byte)2, (byte)2, (byte)2, z, z, z, z, //
                z, z, z, z, z, z, z, z};
            // send ntlm type2 msg

            response.setStatus(response.SC_UNAUTHORIZED);
            String enc2 = new String(java.util.Base64.getMimeEncoder().encode(msg1));
            response.setHeader("WWW-Authenticate", "NTLM "+ enc2.trim());            


               return;
        }
        else if (msg[8] == 3) {
                off = 30;
                length = msg[off+17]*256 + msg[off+16];
                offset = msg[off+19]*256 + msg[off+8];
                s = new String(msg, offset, length);
                // print computer name // out.println(s + " ");
        }
        else
        return;

        length = msg[off+1]*256 + msg[off];
        offset = msg[off+3]*256 + msg[off+2];
        s = new String(msg, offset, length);
        //domain//out.println(s + " ");
        length = msg[off+9]*256 + msg[off+8];
        offset = msg[off+11]*256 + msg[off+10];

        s = new String(msg, offset, length);
        out.println(s);
}
%>