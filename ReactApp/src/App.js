import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';
import Biodata from './Biodata';
import BiodataTable from './BiodataTable';
import Login from './Login';
import ViewBiodata from './ViewBiodata';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import ProtectedRoute from './ProtectedRouter';

function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />

          {/* Admin routes */}
          <Route path="/" element={
            <ProtectedRoute role="ADMIN">
              <BiodataTable />
            </ProtectedRoute>
          } />
          <Route path="/biodata-list" element={
            <ProtectedRoute role="ADMIN">
              <BiodataTable />
            </ProtectedRoute>
          } />
          <Route path="/biodata" element={
            <ProtectedRoute role="ADMIN">
              <Biodata />
            </ProtectedRoute>
          } />
          <Route path="/edit/:id" element={
            <ProtectedRoute role="ADMIN">
              <Biodata />
            </ProtectedRoute>
          } />

          {/* User routes */}
          <Route path="/biodata-view" element={
            <ProtectedRoute role="USER">
              <ViewBiodata />
            </ProtectedRoute>
          } />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;