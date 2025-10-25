import com.cyberbotics.webots.controller.Robot;
import com.cyberbotics.webots.controller.PositionSensor;
import com.cyberbotics.webots.controller.Camera;
import com.cyberbotics.webots.controller.CameraRecognitionObject;
import com.cyberbotics.webots.controller.Motor;

/**
 *
 * @author Dante Sterpin
 */
public class Brazo_Steel_Ro extends Robot
{
    private Camera camera;
    private final int timeStep=32;
    private PositionSensor encoderA[];
    private PositionSensor encoderG[];
    private int height, width, image[];
    private CameraRecognitionObject objData[];
    private String Path = "../../images/Sight.jpg";
    private double sA[],pA[],sG[],pG[];
    private Motor arm[],gripper[];
    private AgenteDifuso agente;
    
    public Brazo_Steel_Ro()
    {
        super();
        
        // Inicializa los sensores
        camera = getCamera("camera");
        camera.enable(4*timeStep);
        camera.recognitionEnable(4*timeStep);
        height = camera.getHeight();
        width = camera.getWidth();
        
        encoderA = new PositionSensor[7];
        for(int a=0; a<7; a++)
        {
            encoderA[a] = getPositionSensor("arm_"+(a+1)+"_joint_sensor");
            encoderA[a].enable(timeStep);
        }
        sA = new double[7];
        
        encoderG = new PositionSensor[2];
        encoderG[0] = getPositionSensor("gripper_left_finger_joint_sensor");
        encoderG[1] = getPositionSensor("gripper_right_finger_joint_sensor");
        
        for(int g=0; g<2; g++)
        {
            encoderG[g].enable(timeStep);
        }
        sG = new double[2];
        
        // Inicializa los motores
        arm = new Motor[7];
        for(int m=0; m<7; m++)
        {
            arm[m] = getMotor("arm_"+(m+1)+"_joint");
        }
        pA = new double[7];
        
        arm[0].setVelocity(1.95 * 0.3);  // Hombro horizontal
        arm[1].setVelocity(1.95 * 0.3);  // Hombro vertical
        arm[2].setVelocity(2.35 * 0.3);  // Brazo superior
        arm[3].setVelocity(2.35 * 0.3);  // Codo
        arm[4].setVelocity(1.95 * 0.3);  // Brazo inferior
        arm[5].setVelocity(1.76 * 0.3);  // Muñeca
        arm[6].setVelocity(1.76 * 0.3);  // Efector
        
        arm[0].setPosition(0.07);  // 0.07 ; 2.68
        arm[1].setPosition(0.0);  // -1.5 ; 1.02
        arm[2].setPosition(0.0);  // -3.46 ; 1.5
        arm[3].setPosition(0.0);  // -0.32 ; 2.29
        arm[4].setPosition(0.0);  // -2.07 ; 2.07
        arm[5].setPosition(0.0);  // -1.39 ; 1.39
        arm[6].setPosition(0.0);  // -2.07 ; 2.07
        
        gripper = new Motor[2];
        gripper[0] = getMotor("gripper_left_finger_joint");
        gripper[1] = getMotor("gripper_right_finger_joint");
        pG = new double[]{0.045, 0.045};
        
        for(int n=0; n<2; n++)
        {
            gripper[n].setVelocity(0.05);
            gripper[n].setPosition(pG[n]);  // 0.0 ; 0.045
        }
        
        // Inicializa los agentes
        agente = new AgenteDifuso();
    }
    
    public void Ejecutar()
    {
        while (step(64) != -1)
        {
            // Lee los sensores
            image = camera.getImage();
            camera.saveImage(Path, 70);
            objData = camera.getCameraRecognitionObjects();
            
            System.out.print("Encoders: ");
            for(int a=0; a<7; a++)
            {
                sA[a] = encoderA[a].getValue();
                System.out.print(sA[a]+" ");
            }
            System.out.print("\n");
            
            for(int g=0; g<2; g++)
            {
                sG[g] = encoderG[g].getValue();
            }
            
            // Decide la actuaci�n
            agente.Razonamiento(objData);
            pA = agente.getArm();
            
            // Mueve los motores
            for(int m=0; m<7; m++)
            {
                arm[m].setPosition(pA[m]);
            }
            
            for(int n=0; n<2; n++)
            {
                gripper[n].setPosition(pG[n]);
            }
        };
        
        finalize();
    }
    
    public static void main(String[] args)
    {
        new Brazo_Steel_Ro().Ejecutar();
    }
}
