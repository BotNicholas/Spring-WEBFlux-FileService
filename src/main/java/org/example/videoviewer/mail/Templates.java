package org.example.videoviewer.mail;

public class Templates {
    private Templates() {}

    public static final String ONE_TIME_CODE_MESSAGE_TEMPLATE = """
            <table style="background-color: #edf4f7; border-radius: 20px; padding: 20px 0; margin: auto; width: 600px;">
                          <tbody>
                              <tr>
                                  <td align="center">
                                      <img src='https://freesvg.org/img/file_server.png' style='width: 20%'>
                                  </td>
                              </tr>
                              <tr>
                                  <td align="center">
                                      <h1>Dear {0}</h1>
                                  </td>
                              </tr>
                              <tr>
                                  <td align="center">
                                      <p style='text-align:center;'>Below you can find one time code that you have to past in your <b>'FIle Server'</b> app to complete your <b>{1}</b> process.</p>
                                  </td>
                              </tr>
                              <tr>
                                  <td align="center">
                                     <div style="display:inline-block;
                                         padding:12px 16px;
                                         border-radius:8px;
                                         background:#111111;
                                         color:#ffffff;
                                         font-family:Consolas, 'Courier New', monospace;
                                         font-size:20px;
                                         line-height:24px;
                                         letter-spacing:3px;">{2}</div>
                                  </td>
                              </tr>
                              <tr>
                                  <td align="center">
                                      <sub><i>Please, don't tell this code anyone!</i></sub>
                                  </td>
                              </tr>
                          </tbody>
                      </table>
            """;
}
