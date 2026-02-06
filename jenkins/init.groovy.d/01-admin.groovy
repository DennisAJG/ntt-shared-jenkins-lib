import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstance()

def user = System.getenv("JENKINS_ADMIN_USER") ?: "admin"
def pass = System.getenv("JENKINS_ADMIN_PASSWORD") ?: "admin"

println(">> Creating local admin user: ${user}")

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount(user, pass)
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

instance.save()
println(">> Admin user created and security enabled.")
