package org.example.videoviewer.mail;

public class Templates {
    private Templates() {}

    public static final String ONE_TIME_CODE_MESSAGE_TEMPLATE = """
            <table style="border-radius: 20px; padding: 20px 0; margin: auto; width: 600px;">
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

    public static final String PASSWORD_RESET_REQUEST_MESSAGE_TEMPLATE = """
            <table style="border-radius: 20px; padding: 20px 0; margin: auto; width: 600px;">
                <tbody>
                    <tr>
                        <td align="center">
                            <img src='https://freesvg.org/img/file_server.png' style='width: 20%%'>
                        </td>
                    </tr>
                    <tr>
                        <td align="center">
                            <h1>Dear %s</h1>
                        </td>
                    </tr>
                    <tr>
                        <td align="center">
                            <p style='text-align:center; color:black;'>Below you can find link to reset your password.</p>
                            <p style='text-align:center; color:black;'>If you didn't request password reset just ignore this message.</p>
                        </td>
                    </tr>
                    <tr>
                        <td align="center">
                            <a href="%s" style="display:inline-block;
                                padding:12px 16px;
                                border-radius:8px;
                                background:#111111;
                                color:#ffffff;
                                font-family:Consolas, 'Courier New', monospace;
                                font-size:20px;
                                line-height:24px;
                                letter-spacing:3px;">%s</a>
                        </td>
                    </tr>
                </tbody>
            </table>
            """;

    public static final String PASSWORD_RESET_MESSAGE_TEMPLATE = """
            <table style="border-radius: 20px; padding: 20px 0; margin: auto; width: 600px;">
                <tbody>
                    <tr>
                        <td align="center">
                            <img src='https://freesvg.org/img/file_server.png' style='width: 20%%'>
                        </td>
                    </tr>
                    <tr>
                        <td align="center">
                            <h1>Dear %s</h1>
                        </td>
                    </tr>
                    <tr>
                        <td align="center">
                            <p style='text-align:center; color:black;'>Your password was successfully reset!</p>
                            <p style='text-align:center; color:black;'>If you didn't reset your password please change it right now or contact us.</p>
                        </td>
                    </tr>
                </tbody>
            </table>
            """;
}
