import ReactFlow, {
  Background,
  Controls,
  MarkerType,
  MiniMap,
  Position,
  type EdgeTypes,
  type NodeTypes,
  type Edge,
  type Node,
} from 'reactflow';
import { graphEdges, graphNodes } from '../../../data/demo';
import 'reactflow/dist/style.css';

type LineageGraphProps = {
  reduceMotion: boolean;
};

const nodes: Node[] = graphNodes.map((node) => ({
  ...node,
  sourcePosition: Position.Right,
  targetPosition: Position.Left,
  draggable: false,
}));

const edges: Edge[] = graphEdges.map((edge) => ({
  ...edge,
  type: 'smoothstep',
  markerEnd: { type: MarkerType.ArrowClosed, width: 18, height: 18, color: '#6ee7b7' },
  style: { stroke: '#6ee7b7', strokeWidth: 1.5 },
  labelStyle: { fill: '#d1fae5', fontWeight: 600 },
}));
const reducedMotionEdges = edges.map((edge) => ({ ...edge, animated: false }));

const nodeTypes: NodeTypes = {};
const edgeTypes: EdgeTypes = {};

export default function LineageGraph({ reduceMotion }: LineageGraphProps) {
  return (
    <div className="h-[420px] w-full overflow-hidden rounded-xl border border-white/[0.08] bg-[#080d0b]">
      <ReactFlow
        nodes={nodes}
        edges={reduceMotion ? reducedMotionEdges : edges}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
        fitView
        fitViewOptions={{ padding: 0.25 }}
        proOptions={{ hideAttribution: true }}
      >
        <MiniMap nodeColor={() => '#34d399'} pannable className="!bg-[#0c1310] !border-white/10" />
        <Controls className="!bg-[#0c1310] !border-white/10 !text-slate-100" />
        <Background gap={20} size={1} color="#1b2922" />
      </ReactFlow>
    </div>
  );
}
