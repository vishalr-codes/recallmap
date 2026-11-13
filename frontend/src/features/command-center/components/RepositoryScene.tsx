import { Canvas } from '@react-three/fiber';
import { Float, MeshDistortMaterial, OrbitControls, RoundedBox } from '@react-three/drei';

type RepositorySceneProps = {
  reduceMotion: boolean;
};

export default function RepositoryScene({ reduceMotion }: RepositorySceneProps) {
  return (
    <div className="h-56 overflow-hidden rounded-xl border border-white/[0.08] bg-[#080d0b]">
      <Canvas camera={{ position: [0, 0, 6], fov: 50 }}>
        <ambientLight intensity={0.9} />
        <pointLight position={[5, 5, 5]} intensity={1.2} color="#34d399" />
        <pointLight position={[-5, -2, 2]} intensity={0.8} color="#7dd3fc" />
        <Float speed={reduceMotion ? 0 : 2.2} rotationIntensity={reduceMotion ? 0 : 0.65} floatIntensity={reduceMotion ? 0 : 0.8}>
          <RoundedBox args={[1.8, 1.2, 1.2]} radius={0.14} position={[-1.3, 0.4, 0]}>
            <MeshDistortMaterial color="#0f766e" emissive="#10b981" emissiveIntensity={0.55} distort={reduceMotion ? 0 : 0.16} speed={reduceMotion ? 0 : 1.5} roughness={0.3} />
          </RoundedBox>
        </Float>
        <Float speed={reduceMotion ? 0 : 2.5} rotationIntensity={reduceMotion ? 0 : 0.75} floatIntensity={reduceMotion ? 0 : 0.9}>
          <RoundedBox args={[1.6, 1, 1]} radius={0.14} position={[0.5, -0.3, 0.4]}>
            <MeshDistortMaterial color="#14532d" emissive="#34d399" emissiveIntensity={0.6} distort={reduceMotion ? 0 : 0.14} speed={reduceMotion ? 0 : 1.7} roughness={0.3} />
          </RoundedBox>
        </Float>
        <Float speed={reduceMotion ? 0 : 2.7} rotationIntensity={reduceMotion ? 0 : 0.8} floatIntensity={reduceMotion ? 0 : 1}>
          <RoundedBox args={[1.3, 0.9, 0.9]} radius={0.14} position={[1.8, 1, -0.5]}>
            <MeshDistortMaterial color="#0d9488" emissive="#22d3ee" emissiveIntensity={0.7} distort={reduceMotion ? 0 : 0.18} speed={reduceMotion ? 0 : 1.8} roughness={0.3} />
          </RoundedBox>
        </Float>
        <OrbitControls enableZoom={false} enablePan={false} autoRotate={!reduceMotion} autoRotateSpeed={0.8} />
      </Canvas>
    </div>
  );
}
