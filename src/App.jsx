import { useApp } from './state/AppContext.jsx'
import Layout from './components/Layout.jsx'
import Login from './pages/Login.jsx'
import Feed from './pages/Feed.jsx'
import RegistrarOcorrencia from './pages/RegistrarOcorrencia.jsx'
import DetalheOcorrencia from './pages/DetalheOcorrencia.jsx'
import Conformidade from './pages/Conformidade.jsx'
import CadastroObra from './pages/CadastroObra.jsx'
import CadastroFuncao from './pages/CadastroFuncao.jsx'
import PainelFiscal from './pages/PainelFiscal.jsx'

function Tela({ tela }) {
  switch (tela.nome) {
    case 'feed': return <Feed />
    case 'registrar': return <RegistrarOcorrencia key="proprio" />
    case 'registrar_terceiro': return <RegistrarOcorrencia key="terceiro" emNomeDeOutro />
    case 'detalhe': return <DetalheOcorrencia ocorrenciaId={tela.ocorrenciaId} />
    case 'conformidade': return <Conformidade />
    case 'obra': return <CadastroObra />
    case 'funcao': return <CadastroFuncao />
    case 'painel': return <PainelFiscal />
    default: return <Feed />
  }
}

export default function App() {
  const { state, usuario } = useApp()
  if (!usuario) return <Login />
  return (
    <Layout>
      <Tela tela={state.tela} />
    </Layout>
  )
}
