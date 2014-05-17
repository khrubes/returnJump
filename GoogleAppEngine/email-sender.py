import smtplib

def createMessage(fromeName, fromEmail, toName, toEmail, subject, body):
    return """From: %s <%s>
To: %s <%s>
Subject: %s

%s
""" % (fromeName, fromEmail, toName, toEmail, subject, body)

sender = 'returnjump@gmail.com'
receivers = ['returnjump@gmail.com']
fromeName = "From Name"
fromEmail = "from@email.com"
toName = "To Name"
toEmail = "to@email.com"
subject = "Python smtplib"
body = "This is a test e-mail message."

message = createMessage(fromeName, fromEmail, toName, toEmail, subject, body)

try:
    smtpObj = smtplib.SMTP('smtp.gmail.com', 587)
    smtpObj.starttls()
    smtpObj.login('returnjump@gmail.com', 'returnpassword')
    smtpObj.sendmail(sender, receivers, message)         
    print "Successfully sent email"
except SMTPException:
    print "Error: unable to send email"