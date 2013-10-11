package problem;

import common.Constants;
import spaceship.Projectile;
import spaceship.Spaceship;

import java.util.ArrayList;
import java.util.List;

public class ProjectileManager {

    public static List<Projectile> projectileList;
    public static boolean suppressNew;


    public static void reset() {
        projectileList = new ArrayList<Projectile>();
    }

    public static Projectile getNewProjectile(Spaceship owner) {
        // get first available dead projectile if one exists
        Projectile created = null;
        if(!suppressNew) {
            for(Projectile p : projectileList) {
                if(!p.alive) {
                    created = p;
                    created.setTeam(owner.team);
                    created.alive = true;
                    created.ttl = Constants.projectileLifetime;
                    break;
                }
            }

            // if projectile still new, make a new one
            if(created == null) {
                created = new Projectile(owner);
                created.setTeam(owner.team);
                projectileList.add(created);
            }
        }
        return created;
    }

    public static void suppressNewProjectiles(boolean suppress) {
        suppressNew = suppress;
    }

    public static List<Projectile> getLivingProjectiles() {
        List<Projectile> livingProjectiles = new ArrayList<Projectile>();
        for(Projectile p : projectileList) {
            if(p.alive) {
                livingProjectiles.add(p);
            }
        }
        return livingProjectiles;
    }

}
